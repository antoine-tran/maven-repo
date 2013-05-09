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
package edu.umd.cloud9.collection.wikipedia;


import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import edu.umd.cloud9.io.pair.PairOfStringInt;

/**
 * Tool for extracting the weighted link graph out of the Wikipedia corpus.
 * Each node in the graph represents a Wikipedia article, each edge is a link
 * between two articles, and the weight of the link correspond to the number
 * of times the destination article appears in the content of the source 
 * article.
 * <br>
 * 
 * The graph is represented as adjacency lists, each single line
 * contains a source node Id, list of its outgoing links with the weights. 
 * <br>
 * One sample invocation of this tool:<br>
 * <code>
 * hadoop jar cloud9-[version].jar edu...BuildWikipediaWeightedLinkGraph 
 * -input [wikipedia XML Dump file] -reduceNo [reducer count] -phase 3
 * </code>
 * @author Tuan
 * @version 0.2
 * @since 13 May 2012
 *
 */
public class BuildWikipediaWeightedLinkGraph extends 
		Configured implements Tool {

	private static final Logger log = 
			Logger.getLogger(BuildWikipediaWeightedLinkGraph.class);

	private static final String LANG_OPTION = "lang";
	private static final String INPUT_OPTION = "input";
	private static final String REDUCE_NO = "reduce";
	private static final String PHASE = "phase";

	/** 
	 * Parse each Wikipedia article and emit the tuples (outgoingLinks, no),
	 * where outgoingLinks is the title of one destination article, and no is
	 * the number of times the destination article is linked from the source
	 * article.
	 * 
	 * The output is of the form (String1, <String2, int>), where keys are the
	 * title of the destination node, and the content of values vary:
	 * - IF String1 = String2, then int will be the pageId
	 * - IF String1 <> String2 and int is -1, then String1 is a redirect page
	 * and every other incoming links should be redirected to the page String2
	 * - IF String1 <> String2 and int is positive, then every other incoming
	 * links should be updated with the page id of String1
	 *  
	 * We apply some "quick-and-dirty" hacks to pass the Wikipedia article
	 * info along with other link messages:
	 * -
	 */
	private static class LinkEmitMapClass extends 
	Mapper<LongWritable, WikipediaPage, Text, PairOfStringInt> {

		private Text newKey = new Text();
		private PairOfStringInt pair = new PairOfStringInt();
		private Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<String>();

		@Override
		protected void map(LongWritable key, WikipediaPage p, Context context)
				throws IOException, InterruptedException {

			// only articles are emitted
			boolean redirected = false;
			if (p.isRedirect()) {
				redirected = true;
			} else if (!p.isArticle()) return;		
			map.clear();
			String title = p.getTitle().trim();

			// to make the title case-sensitive, we will change all lower-cased
			// first characters to upper-case.
			if (title.isEmpty()) return;
			String fc = title.substring(0, 1);
			if (fc.matches("[a-z]")) {
				title = title.replaceFirst(fc, fc.toUpperCase());				
			}

			// do not pass the structure message of a redirect article
			if (!redirected) {
				newKey.set(title);
				int id = Integer.parseInt(p.getDocid());
				pair.set(title, id);
				context.write(newKey, pair);	
			}			

			for (String t : p.extractLinkDestinations()) {
				t = t.trim();
				if (t.isEmpty()) continue;
				fc = t.substring(0, 1);
				if (fc.matches("[a-z]")) {
					t = t.replaceFirst(fc, fc.toUpperCase());
				}
				if (title.equals(t)) continue;
				if (redirected) {
					newKey.set(title);
					pair.set(t, -1);
					context.write(newKey, pair);
					return;
				} else {			
					if (!map.containsKey(t)) {
						map.put(t, 1);
					}
					else {
						int v = map.getInt(t);
						map.put(t, v + 1);
					}
				}
			}
			String[] keys = map.keySet().toArray((new String[map.size()]));

			for (String k : keys) {
				if (k.isEmpty()) continue;				
				newKey.set(k);
				int cnt = map.get(k);
				pair.set(title, cnt);
				context.write(newKey, pair);
			}		
		}		
	}

	/**
	 * aggregate all incoming links for a particular article, detect redirect
	 * links and update them with actual article ids. Links will not be emitted
	 * further if the node is not an actual article (redirect, stub, ....)
	 * */ 
	private static class RedirectResolveReduceClass extends 
	Reducer<Text, PairOfStringInt, Text, PairOfStringInt> {

		private PairOfStringInt newPair = new PairOfStringInt();
 
		@Override
		protected void reduce(Text key, Iterable<PairOfStringInt> values, 
				Context context) throws IOException, InterruptedException {

			// the sentinel indicating whether we encounter the structure or
			// redirect message along the iterator
			Text newKey = null;
			boolean redirected = false;
			List<PairOfStringInt> cache = new ArrayList<PairOfStringInt>();
			PairOfStringInt tmpItem;

			// internal counter for debugging
			int v, linkCnt = 0, totalCnt = 0;

			// a sample page to trace ill-formed articles
			String k, tmpPage = null;

			log.info("Processing page: " + key.toString());
			for (PairOfStringInt pair : values) {
				k = pair.getKey();
				v = pair.getValue();
				totalCnt++;
				// look for redirect message first
				if (v == -1) {
					newKey = new Text(k);
					redirected = true;
					log.info("redirect message: (" + newKey + ", <" + k + ", "
							+ v + ">)");
				} 

				// then look for structure message
				else if (key.toString().equals(k)) {

					// there can be some redirect articles that redirect to 
					// themselves. Those articles should be ignored
					if (!redirected) {
						newKey = key;
						newPair.set(k, v);
						context.write(newKey, newPair);
						log.info("structure message: (" + key + ", <" + k +
								", " + v + ">)");	
					} else return;
				} 

				// items before the redirect or structure messages in 
				// the iterator will be copied and be emitted later
				else if (newKey == null) {
					tmpItem = new PairOfStringInt(k, v);
					cache.add(tmpItem);

					// pick one article to debug in case the key is indeed an 
					// ill-formed article
					if (tmpPage == null) {
						tmpPage = k;
					}
				} 

				// items after the redirect or structure messages will be
				// emitted right away. Boundary (possibly never happened)
				// case: When page p1 is a redirect to p2, and p2 has links
				// to p1, then the link should be ignored
				else if (!newKey.toString().equals(k)) {
					linkCnt++;
					newPair.set(k, v);
					context.write(newKey, newPair);
				} else {
					log.warn("Weird! " + k + " links to its redirect.");
				}
			}

			// The ill-formed link
			if (newKey == null) {
				log.info("Ill-formed link: " + key + ", found in " + tmpPage);
				return;
			}

			// second run: update the remaining links with actual destination
			for (PairOfStringInt pair: cache) {
				linkCnt++;
				context.write(newKey, pair);
			}
			log.info("Page: " + key + ". Total count: " + totalCnt + 
					", passed link count: " + linkCnt);
		}		
	}

	private static class DestinationIdResolveReduceClass extends 
	Reducer<Text, PairOfStringInt, Text, PairOfStringInt> {

		Text newKey = new Text();
		PairOfStringInt newValue = new PairOfStringInt();

		@Override
		protected void reduce(Text key, Iterable<PairOfStringInt> values,
				Context context) throws IOException, InterruptedException {
			String k, id = null;
			int v;
			List<PairOfStringInt> cache = new ArrayList<PairOfStringInt>();
			PairOfStringInt tmpItem;
			for (PairOfStringInt pair : values) {
				k = pair.getKey();
				v = pair.getValue();
				newKey.set(k);

				// find the structure message
				if (key.toString().equals(k)) {
					id = String.valueOf(v);
					newValue.set(k, v);
					context.write(newKey, newValue);
				} 

				// messages after the structure message will be emitted 
				// immediately
				else if (id != null) {
					newValue.set(id, v);
					context.write(newKey, newValue);
				} 

				// messages before the structure message will be copied
				// and emitted later
				else {
					tmpItem = new PairOfStringInt(k, v);
					cache.add(tmpItem);
				}
			}
			if (id == null) {
				log.warn("Still found ill-formed article: " + key);
			} else for (PairOfStringInt pair : cache) {
				k = pair.getKey();
				newKey.set(k);
				newValue.set(id, pair.getValue());
				context.write(newKey, newValue);
			}
		}		
	}

	private static class SourceIdResolveReduceClass extends 
			Reducer<Text, PairOfStringInt, Text, Text> {
		
		Text newKey = new Text();
		Text newValue = new Text();
		Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<String>();

		@Override
		protected void reduce(Text key, Iterable<PairOfStringInt> values,
				Context context) throws IOException, InterruptedException {
			map.clear();
			String k, id = null;
			int v;
			for (PairOfStringInt pair : values) {
				k = pair.getKey();
				v = pair.getValue();
				
				// find structure message, and accumulate the others
				if (key.toString().equals(k)) {
					id = String.valueOf(v);
				} else {
					if (!map.containsKey(k)) map.put(k, v);
					else {
						int value = map.getInt(k);
						map.put(k, value + v);
					}
				}
			}
			if (id == null) {
				log.warn("A page having no structure message: " + key);
			} else {
				newKey.set(id);
				String[] keys = map.keySet().toArray(new String[map.size()]);
				for (String dest : keys) {
					newValue.set("\t" + dest + "\t" + map.get(dest));
					context.write(newKey, newValue);
				}
			}
		}
	}

	private String phase1(String inputPath, int reduceNo, String lang) throws 
	IOException, InterruptedException, ClassNotFoundException {

		String output = "tmp/wiki-link/phase1";

		Job job = new Job(getConf(), 
				"Build Wikipedia Weighted Link Graph. Phase 1");
		job.setJarByClass(BuildWikipediaWeightedLinkGraph.class);
		job.getConfiguration().setBoolean(
				"mapred.map.tasks.speculative.execution", false);
		job.getConfiguration().setBoolean(
				"mapred.reduce.tasks.speculative.execution", false);
		job.getConfiguration().set("mapred.child.java.opts", "-Xmx2048m");

		job.setNumReduceTasks(reduceNo);

		FileInputFormat.setInputPaths(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(output));

		if ("en".equals(lang)) {
			job.setInputFormatClass(EnglishWikipediaPageInputFormat.class);
		}
		else throw new InterruptedException("Wikipedia dump with language " + lang + " is not supported ");
		
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(PairOfStringInt.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(PairOfStringInt.class);

		job.setMapperClass(LinkEmitMapClass.class);	
		job.setReducerClass(RedirectResolveReduceClass.class);

		job.waitForCompletion(true);

		return output;
	}

	private String phase2(String inputPath, int reduceNo) throws 
	IOException, InterruptedException, ClassNotFoundException {

		String output = "tmp/wiki-link/phase2";

		Job job = new Job(getConf(), 
				"Build Wikipedia Weighted Link Graph. Phase 2");
		job.setJarByClass(BuildWikipediaWeightedLinkGraph.class);
		job.getConfiguration().setBoolean(
				"mapred.map.tasks.speculative.execution", false);
		job.getConfiguration().setBoolean(
				"mapred.reduce.tasks.speculative.execution", false);
		job.getConfiguration().set("mapred.child.java.opts", "-Xmx2048m");

		job.setNumReduceTasks(reduceNo);

		FileInputFormat.setInputPaths(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(output));

		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(PairOfStringInt.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(PairOfStringInt.class);

		job.setReducerClass(DestinationIdResolveReduceClass.class);

		job.waitForCompletion(true);

		return output;
	}
	
	private String phase3(String inputPath, int reduceNo) throws 
			IOException, InterruptedException, ClassNotFoundException {

		String output = "trace/phase3";

		Job job = new Job(getConf(), 
				"Build Wikipedia Weighted Link Graph. Phase 3");
		job.setJarByClass(BuildWikipediaWeightedLinkGraph.class);
		job.getConfiguration().setBoolean(
				"mapred.map.tasks.speculative.execution", false);
		job.getConfiguration().setBoolean(
				"mapred.reduce.tasks.speculative.execution", false);
		job.getConfiguration().set("mapred.child.java.opts", "-Xmx2048m");

		job.setNumReduceTasks(reduceNo);

		FileInputFormat.setInputPaths(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(output));

		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(PairOfStringInt.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setReducerClass(SourceIdResolveReduceClass.class);

		job.waitForCompletion(true);

		return output;
	}

	@SuppressWarnings("static-access")
	public int run(String[] args) throws Exception {
		Options opts = new Options();

		Option langOpt = OptionBuilder.withArgName("lang")
				.hasArg().withDescription("language of the Wikipedia dump file")
				.create(LANG_OPTION);
		
		Option inputOpt = OptionBuilder.withArgName("input-path")
				.hasArg().withDescription("XML dump file path")
				.create(INPUT_OPTION);

		Option reduceOpt = OptionBuilder.withArgName("reduce-no")
				.hasArg().withDescription("numer of reducer nodes")
				.create(REDUCE_NO);

		Option phaseOpt = OptionBuilder.withArgName("phase-no")
				.hasArg().withDescription("numer of reducer nodes")
				.create(PHASE);

		opts.addOption(langOpt);
		opts.addOption(inputOpt);
		opts.addOption(reduceOpt);
		opts.addOption(phaseOpt);

		CommandLine cl;
		CommandLineParser parser = new GnuParser();
		try {
			cl = parser.parse(opts, args);	
		} catch (ParseException e) {
			System.err.println("Error parsing command line: " + 
					e.getMessage());
			return -1;
		}		
		if (!cl.hasOption(INPUT_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(getClass().getName(), opts);
			ToolRunner.printGenericCommandUsage(System.out);
			return -1;
		}
		int reduceNo = 1;
		if (cl.hasOption(REDUCE_NO)) {
			try {
				reduceNo = Integer.parseInt(cl.getOptionValue(REDUCE_NO));	
			} catch (NumberFormatException e) {
				System.err.println("Error parsing reducer number: " + 
						e.getMessage());
			}			
		}
		int phase = 1;
		if (cl.hasOption(PHASE)) {
			try {
				phase = Integer.parseInt(cl.getOptionValue(PHASE));	
			} catch (NumberFormatException e) {
				System.err.println("Error parsing phase number: " + 
						e.getMessage());			
			}			
		}
		String lang = "en";
		if (cl.hasOption(LANG_OPTION)) {
			lang = cl.getOptionValue(LANG_OPTION);
		}
		
		String input = cl.getOptionValue(INPUT_OPTION);
		if (phase == 1) {
			phase1(input, reduceNo, lang);
		} else if (phase == 2) {
			String output = phase1(input, reduceNo, lang);
			phase2(output, reduceNo);
		} else if (phase == 3) {
			String path = phase1(input, reduceNo, lang);
			path = phase2(path, reduceNo);
			path = phase3(path, reduceNo);
		}
		return 0;
	}	

	public static void main(String[] args) throws Exception {
		ToolRunner.run(new BuildWikipediaWeightedLinkGraph(), args);
	}
}
