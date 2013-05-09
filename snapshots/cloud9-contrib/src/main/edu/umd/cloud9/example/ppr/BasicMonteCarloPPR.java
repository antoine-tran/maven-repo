/*
 * Cloud9: A MapReduce Library for Hadoop
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package edu.umd.cloud9.example.ppr;

import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;

import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import tuan.hadoop.io.IntFloatArrayListWritable;
import tuan.hadoop.io.IntFloatListWritable;
import tuan.hadoop.io.IntBitSetWritable;
import tuan.hadoop.io.IntSetWritable;
import tuan.math.CategoricalRandom;
import tuan.math.DiscreteRandom;
import tuan.math.InvalidParameterException;
import tuan.math.ParameterAlreadyDeclaredException;
import tuan.math.ParameterNotDeclaredException;
import edu.umd.cloud9.example.ppr.FingerPrint.Type;
import edu.umd.cloud9.mapreduce.lib.input.NonSplitableSequenceFileInputFormat;


/**
 * An extension of the library Cloud9, which implements a fully personalized
 * PageRank algorithm. This algorithm is naive and assumes:
 * - the graph fits into main memory; 
 * - each Reducer is responsible for the entire personalized pageranks in
 * one Monte Carlo approximation. As such, there are n reducers for n random
 * walks; 
 * - the graph is represented as plain-text files, no compression technique 
 * is applied. Each line corresponds to a node and its adjacency list; 
 * - InputFormat is NLineInputFormat
 * 
 * @author tuan
 * @version 0.1
 *
 */
public class BasicMonteCarloPPR extends Configured implements Tool {

	private static final Logger LOG = 
			Logger.getLogger(BasicMonteCarloPPR.class);

	/** This enum keeps track of counters that control the behavior 
	 * of the algorithm* /
	 * @author work
	 *
	 */
	private static enum COUNTERS {
		FINISHED_RW, TOTAL_RW
	}

	/*
	 * A Mapper without In-mapper combining design patterns. 
	 * @author tuan
	 *
	 */
	private static class RandomWalkInitMapClass extends 
	Mapper<LongWritable, Text, IntSetWritable, FingerPrint> {

		@Override
		protected void map(LongWritable key, Text t, Context context)
				throws IOException, InterruptedException {		
			String[] tokens = t.toString().trim().split("\\s+");

			// Get the number of random walks from the input
			int num = context.getConfiguration().getInt("RandomWalks", 100);

			// Count the number of random walks that will be performed
			int totalCnt = 0;

			// empty line --> log but move on
			if (tokens == null || tokens.length == 0) {
				LOG.warn("empty line encountered");
			}
			int beginId = Integer.parseInt(tokens[0]);			
			FingerPrint node, edge;
			IntSetWritable newKey;

			if (tokens.length > 0) {
				LOG.info("orphan node: " + beginId);
				node = new FingerPrint();
				node.setBeginId(beginId);
				node.setEndId(beginId);
				node.setType(Type.STRUCTURE);
				node.setLength(0);
				int n = tokens.length;

				// build the adjacency list
				IntFloatListWritable adjacencyLst = 
						new IntFloatArrayListWritable(n / 2);				
				for (int i = 0; i < n - 1; i+= 2) {
					int nodeId = Integer.parseInt(tokens[i]);
					float weight = Float.parseFloat(tokens[i + 1]);
					adjacencyLst.add(i / 2, nodeId, weight);
				}				
				node.setAdjacencyList(adjacencyLst);
				for (int i = 0; i < num; i++) {

					// emit the graph structure first
					newKey = new IntBitSetWritable();

					// The order is the key here
					newKey.add(i);
					newKey.add(beginId);					
					context.write(newKey, node);

					// then, emit the edges
					for (int j = 0, m = adjacencyLst.size(); j < m; j++) {
						edge = new FingerPrint();
						edge.setBeginId(beginId);
						edge.setEndId(adjacencyLst.getIndex(j));
						edge.setType(Type.FINGERPRINT);
						edge.setLength(1);

						// again, the order (iteration number --> ending node)
						// is the key, it makes the join for next map-reduce
						// phase possible
						newKey = new IntBitSetWritable();
						newKey.add(i);
						newKey.add(adjacencyLst.getIndex(j));
					}
				}

				// update the global counter of total random walks
				totalCnt += num;
				context.getCounter(COUNTERS.TOTAL_RW).increment(totalCnt);
			}				
		}
	}

	private static class RandomWalkReduceClass extends 
	Reducer<IntSetWritable, FingerPrint, IntSetWritable, FingerPrint>{

		private Random coin = new Random();
		private DiscreteRandom dr = new CategoricalRandom();

		@Override
		protected void reduce(IntSetWritable key, Iterable<FingerPrint> fps,
				Context context) throws IOException, InterruptedException {			

			// fetch the current iteration
			int iterNo = key.toArray()[0];

			// counter to control the termination of MapReduce iterations. 
			// The iterations will be terminated when the counter reaches
			// N x M, where N is the number of independent random walks 
			// and M is the number of graph nodes.
			int internalCnt = 0;

			FingerPrint newValue;
			IntSetWritable newKey;
			IntFloatListWritable adjacencyLst = null;

			// Get the jumping factor from the the input
			float df = context.getConfiguration().
					getFloat("danglingFactor", 0.25f);

			// This light-weighed index is used to keep track of finished
			// random walks
			IntSet finished = new IntOpenHashSet();

			// in the first run, iterate over the finger prints to find
			// the structure		
			for (FingerPrint fp : fps) {

				// Received a random walk, decide if it should be extended
				if (fp.getType() == Type.FINGERPRINT) {

					// dangle: Stop and emit the finished walk
					if (coin.nextFloat() < df) {

						// mark the finished random walk
						finished.add(fp.getBeginId());

						// increment the internal counter
						internalCnt++;

						// return new key
						newKey = new IntBitSetWritable();
						newKey.add(iterNo);
						newKey.add(fp.getBeginId());

						// new value
						newValue = new FingerPrint();
						newValue.setBeginId(fp.getBeginId());
						newValue.setEndId(newValue.getEndId());
						newValue.setLength(fp.getLength());
						newValue.setType(Type.FINISHED);

						context.write(newKey, newValue);
					}
				}

				// Received a graph structure message
				if (fp.getType() == Type.STRUCTURE) {
					adjacencyLst = fp.getAdjacencyList();
					float[] weights = adjacencyLst.contents();
					try {
						dr.parameterize(weights);
					} catch (ParameterAlreadyDeclaredException e) {
						throw new InterruptedException(e.getMessage());
					} catch (InvalidParameterException e) {
						throw new InterruptedException(e.getMessage());
					}
				}
			}

			// After the first run, if not received any graph structure
			// --> raise an exception and interrupt
			if (dr == null) {
				throw new InterruptedException(
						"Graph structure message not found");
			}

			// Second run, extend the unfinished random walks
			for (FingerPrint fp : fps) {
				if (fp.getType() == Type.FINGERPRINT) {
					int beginId = fp.getBeginId();
					if (!finished.contains(beginId)) {
						try {
							// randomly choose a neighbor to extend the random walk
							int i = dr.nextInt();
							int randomNeighbor = adjacencyLst.getIndex(i);

							// generate a new key for the extended random walk
							newKey = new IntBitSetWritable();
							newKey.add(iterNo);
							newKey.add(randomNeighbor);

							// now generate the new value. Everything except
							// the ending node stays unchanged, and the length
							// increases by 1
							newValue = new FingerPrint();
							newValue.setBeginId(fp.getBeginId());
							newValue.setEndId(randomNeighbor);
							newValue.setLength(fp.getLength() + 1);
							newValue.setType(Type.FINGERPRINT);

							context.write(newKey, newValue);
						} catch (ParameterNotDeclaredException e) {
							throw new InterruptedException(e.getMessage());
						}
					}
				}
			}

			// update the global counter of finished random walks
			context.getCounter(COUNTERS.FINISHED_RW).increment(internalCnt);
		}		

	}

	private static class RandomWalkFinalizeMapClass extends
	Mapper<IntSetWritable, FingerPrint, IntWritable, IntWritable> {

		@Override
		protected void map(IntSetWritable key, FingerPrint value,
				Context context) throws IOException, InterruptedException {

			if (value.getType() != Type.FINISHED) {
				throw new InterruptedException("Phase 1 is not yet finished");
			}
			int[] items = key.toArray();
			int beginId = items[1];
			IntWritable newKey = new IntWritable(beginId);
			IntWritable newValue = new IntWritable(value.getEndId());
			context.write(newKey, newValue);
		}
	}

	private static class RandomWalkFinalizeReduceClass extends
	Reducer<IntWritable, IntWritable, IntWritable, Text> {

		@Override
		protected void reduce(IntWritable key, Iterable<IntWritable> vals,
				Context context)
						throws IOException, InterruptedException {
			int keyInt = key.get();
			StringBuilder s = new StringBuilder();
			s.append(keyInt);
			s.append("\t");
			Int2IntAVLTreeMap ppr = new Int2IntAVLTreeMap();
			int totalCnt = 0;
			for (IntWritable node : vals) {
				int nodeId = node.get();
				if (!ppr.containsKey(nodeId)) {
					ppr.put(nodeId, 1);
				} else {
					int oldVal = ppr.get(nodeId);
					ppr.put(nodeId, oldVal + 1);
				}
				totalCnt++;
			}
			ObjectSortedSet<Int2IntMap.Entry> entries = ppr.int2IntEntrySet();
			ObjectBidirectionalIterator <Int2IntMap.Entry> iters = 
					entries.iterator();
			while (iters.hasNext()) {
				Int2IntMap.Entry entry = iters.next();
				s.append(entry.getIntKey());
				float freq = ((float)entry.getIntValue() / totalCnt);
				s.append("\t");
				s.append(freq);
				s.append("\t");
			}
			s.append("\n");
			context.write(key, new Text(s.toString()));
		}
	}

	private long phase1(int iterNum, String basePath, String inputFile) 
			throws IOException, InterruptedException, ClassNotFoundException {
		Job job = new Job(getConf(), "Phase 1: Initialize the " + iterNum +
				" random walks");
		job.setJarByClass(BasicMonteCarloPPR.class);
		String in = basePath + "/input/" + inputFile;
		String out = basePath + "/tmp/iter0";
		int reduceNo = iterNum;

		job.getConfiguration().setInt("RandomWalks", iterNum);
		job.getConfiguration()
			.setBoolean("mapred.map.tasks.speculative.execution", false);
		job.getConfiguration()
			.setBoolean("mapred.reduce.tasks.speculative.execution", false);
		job.getConfiguration().set("mapred.child.java.opts", "-Xmx2048m");

		job.setNumReduceTasks(reduceNo);

		FileInputFormat.setInputPaths(job, new Path(in));
		FileOutputFormat.setOutputPath(job, new Path(out));

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(FingerPrint.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(FingerPrint.class);

		job.setMapperClass(RandomWalkInitMapClass.class);
		job.setReducerClass(RandomWalkReduceClass.class);

		job.waitForCompletion(true);
		
		Counters counters = job.getCounters();		
		return counters.findCounter(COUNTERS.TOTAL_RW).getValue();
	}

	private long phase2(int reduceNo, int iterCnt, 
			String basePath, float jumpFactor) throws
			IOException, InterruptedException, ClassNotFoundException {

		Job job = new Job(getConf(), "Phase 2: Processing the " + 
				(iterCnt + 1) + 
				"-th iterations of random walk Monte Carlo approximation");
		job.setJarByClass(BasicMonteCarloPPR.class);
		String in = basePath + "/tmp/iter" + iterCnt;
		String out = basePath + "/tmp/iter" + (iterCnt + 1);

		job.getConfiguration()
			.setBoolean("mapred.map.tasks.speculative.execution", false);
		job.getConfiguration()
			.setBoolean("mapred.reduce.tasks.speculative.execution", false);
		job.getConfiguration().set("mapred.child.java.opts", "-Xmx2048m");

		job.setNumReduceTasks(reduceNo);

		FileInputFormat.setInputPaths(job, new Path(in));
		FileOutputFormat.setOutputPath(job, new Path(out));

		job.setInputFormatClass(NonSplitableSequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		job.setMapOutputKeyClass(IntSetWritable.class);
		job.setMapOutputValueClass(FingerPrint.class);

		job.setOutputKeyClass(IntSetWritable.class);
		job.setOutputValueClass(FingerPrint.class);

		job.setReducerClass(RandomWalkReduceClass.class);

		job.waitForCompletion(true);

		Counters counters = job.getCounters();
		
		return counters.findCounter(COUNTERS.FINISHED_RW).getValue(); 
	}

	private void phase3(int reduceNo, String basePath, int iterNumber, 
			String outPath) throws 
			IOException, InterruptedException, ClassNotFoundException {
		Job job = new Job(getConf(), "Phase 3: Approximate the PPR");
		job.setJarByClass(BasicMonteCarloPPR.class);
		String in = basePath + "/tmp/iter" + iterNumber;
		String out = basePath + "/output/" + outPath;

		job.getConfiguration()
			.setBoolean("mapred.map.tasks.speculative.execution", false);
		job.getConfiguration()
			.setBoolean("mapred.reduce.tasks.speculative.execution", false);
		job.getConfiguration().set("mapred.child.java.opts", "-Xmx2048m");

		job.setNumReduceTasks(reduceNo);

		FileInputFormat.setInputPaths(job, new Path(in));
		FileOutputFormat.setOutputPath(job, new Path(out));

		job.setInputFormatClass(NonSplitableSequenceFileInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(IntWritable.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(RandomWalkFinalizeMapClass.class);
		job.setReducerClass(RandomWalkFinalizeReduceClass.class);

		job.waitForCompletion(true);
	}
	
	@Override
	public int run(String[] args) throws Exception {
		if (args.length != 7) {
			return printUsage();
		}

		String basePath = args[0];
		String inputFile = args[1];
		String outputFile = args[2];
		float jumpingFactor = Float.parseFloat(args[3]);		
		int monteCarloIterNum = Integer.parseInt(args[4]);

		// initialize the random walks from plain-text encoded graph
		long totalRWs = phase1(monteCarloIterNum, basePath, inputFile);
		
		// iteratively perform the random walks until finished	
		int iterNo = 0;
		long finishedRWs = 0l;
		
		while (finishedRWs < totalRWs) {
			finishedRWs = phase2(monteCarloIterNum, iterNo, basePath, 
					jumpingFactor);
			iterNo++;
		}
		
		// finalize the PPR
		phase3(monteCarloIterNum, basePath, iterNo, outputFile);
		
		return 0;
	}

	private static int printUsage() {
		System.out.println("usage: [basePath] [numNodes] [start] [end]" +
				" [useCombiner?] [useInMapCombiner?] [useRange?]");
		ToolRunner.printGenericCommandUsage(System.out);
		return -1;
	}
	
	/**
	 * Dispatches command-line arguments to the tool via the
	 * <code>ToolRunner</code>.
	 */
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), 
				new BasicMonteCarloPPR(), args);
		System.exit(res);
	}

	public BasicMonteCarloPPR() {
	}
}
