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

import java.io.IOException;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import edu.umd.cloud9.collection.wikipedia.WikipediaPage;
import edu.umd.cloud9.collection.wikipedia.WikipediaPage.ContextedLink;
import edu.umd.cloud9.collection.wikipedia.WikipediaPage.Link;
import edu.umd.cloud9.io.map.HMapSIW;
import edu.umd.cloud9.io.pair.PairOfIntString;
import edu.umd.cloud9.io.pair.PairOfStringInt;
import edu.umd.cloud9.io.pair.PairOfStrings;

/**
 * Tool for extracting anchor text out of Wikipedia.
 * 
 * @author Jimmy Lin
 * 
 * @since 29.05.2014 - Tuan: Refactor to the new mapreduce framework
 * TODO: Add support for text output (29.05.2014)
 */
public class ExtractWikipediaAnchorTextWithWindow extends Configured implements Tool {
	private static final Logger LOG = Logger.getLogger(ExtractWikipediaAnchorTextWithWindow.class);

	private static enum PageTypes {
		TOTAL, REDIRECT, DISAMBIGUATION, EMPTY, ARTICLE, STUB, NON_ARTICLE
	};

	private static class MyMapper1 extends Mapper<IntWritable, WikipediaPage, 
	PairOfStringInt, PairOfStrings> {
		private static final PairOfStringInt KEYPAIR = new PairOfStringInt();
		private static final PairOfStrings VALUEPAIR = new PairOfStrings();

		// Basic algorithm:
		// Emit: key = (link target article name, 0), value = (link target docid, "");
		// Emit: key = (link target article name, 1), value = (src docid, anchor text with context, offset and length)
		public void map(IntWritable key, WikipediaPage p, Context context) 
				throws IOException, InterruptedException {
			context.getCounter(PageTypes.TOTAL).increment(1);

			String title = p.getTitle();

			// This is a caveat and a potential gotcha: Wikipedia article titles are not case sensitive on
			// the initial character, so a link to "commodity" will go to the article titled "Commodity"
			// without any issue. Therefore we need to emit two versions of article titles.

			VALUEPAIR.set(p.getDocid(), "");
			KEYPAIR.set(title, 0);
			context.write(KEYPAIR, VALUEPAIR);

			String fc = title.substring(0, 1);
			if (fc.matches("[A-Z]")) {
				title = title.replaceFirst(fc, fc.toLowerCase());

				KEYPAIR.set(title, 0);
				context.write(KEYPAIR, VALUEPAIR);
			}

			if (p.isRedirect()) {
				context.getCounter(PageTypes.REDIRECT).increment(1);
			} else if (p.isDisambiguation()) {
				context.getCounter(PageTypes.DISAMBIGUATION).increment(1);
			} else if (p.isEmpty()) {
				context.getCounter(PageTypes.EMPTY).increment(1);
			} else if (p.isArticle()) {
				context.getCounter(PageTypes.ARTICLE).increment(1);

				if (p.isStub()) {
					context.getCounter(PageTypes.STUB).increment(1);
				}
			} else {
				context.getCounter(PageTypes.NON_ARTICLE).increment(1);
			}

			for (ContextedLink link : p.extractContextedLinks()) {
				KEYPAIR.set(link.getTarget(), 1);
				VALUEPAIR.set(p.getDocid(), link.getContext());

				context.write(KEYPAIR, VALUEPAIR);
			}
		}
	}

	private static class MyReducer1 extends Reducer<PairOfStringInt, 
	PairOfStrings, IntWritable, PairOfIntString> {
		private static final IntWritable SRCID = new IntWritable();
		private static final PairOfIntString TARGET_ANCHOR_PAIR 
		= new PairOfIntString();

		private String targetTitle;
		private int targetDocid;



		public void reduce(PairOfStringInt key, Iterable<PairOfStrings> values,
				Context context) throws IOException, InterruptedException {

			if (key.getRightElement() == 0) {
				targetTitle = key.getLeftElement();
				for (PairOfStrings pair : values) {
					targetDocid = Integer.parseInt(pair.getLeftElement());
					break;
				}
			} else {
				if (!key.getLeftElement().equals(targetTitle)) {
					return;
				}

				for (PairOfStrings pair : values) {
					SRCID.set(Integer.parseInt(pair.getLeftElement()));
					TARGET_ANCHOR_PAIR.set(targetDocid, pair.getRightElement());

					context.write(SRCID, TARGET_ANCHOR_PAIR);
				}
			}
		}
	}

	private static class MyPartitioner1 extends Partitioner<PairOfStringInt, PairOfStrings> {
		public int getPartition(PairOfStringInt key, PairOfStrings value, int numReduceTasks) {
			return (key.getLeftElement().hashCode() & Integer.MAX_VALUE) % numReduceTasks;
		}
	}

	private static class MyMapper2 extends Mapper<IntWritable, PairOfIntString,
	IntWritable, Text> {
		private static final IntWritable KEY = new IntWritable();
		private static final Text VALUE = new Text();

		public void map(IntWritable key, PairOfIntString t, Context context) 
				throws IOException, InterruptedException {
			KEY.set(t.getLeftElement());
			VALUE.set(t.getRightElement());

			context.write(KEY, VALUE);
		}
	}

	private static class MyReducer2 extends Reducer<IntWritable,
	Text, IntWritable, HMapSIW> {
		private static final HMapSIW map = new HMapSIW();

		public void reduce(IntWritable key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {
			map.clear();

			for (Text cur : values) {
				map.increment(cur.toString());
			}

			context.write(key, map);
		}
	}

	private static final String INPUT_OPTION = "input";
	private static final String OUTPUT_OPTION = "output";
	private static final String REDIR_OPTION = "redirect";

	@SuppressWarnings("static-access")
	@Override
	public int run(String[] args) throws Exception {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("path").hasArg()
				.withDescription("input").create(INPUT_OPTION));
		options.addOption(OptionBuilder.withArgName("redirect").hasArg()
				.withDescription("redirect option").create(REDIR_OPTION));
		options.addOption(OptionBuilder.withArgName("path").hasArg()
				.withDescription("output for adjacency list").create(OUTPUT_OPTION));

		CommandLine cmdline;
		CommandLineParser parser = new GnuParser();
		try {
			cmdline = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("Error parsing command line: " + exp.getMessage());
			return -1; 
		}

		if (!cmdline.hasOption(INPUT_OPTION) || !cmdline.hasOption(OUTPUT_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(this.getClass().getName(), options);
			ToolRunner.printGenericCommandUsage(System.out);
			return -1;
		}

		Random random = new Random();
		String tmp = "tmp-" + this.getClass().getCanonicalName() + "-" + random.nextInt(10000);

		// task1(cmdline.getOptionValue(INPUT_OPTION), tmp);
		task1(cmdline.getOptionValue(INPUT_OPTION), cmdline.getOptionValue(OUTPUT_OPTION));

		/* if (!cmdline.hasOption(REDIR_OPTION)) {
			task2(tmp, cmdline.getOptionValue(OUTPUT_OPTION));
		}
		else {
			String tmp2 = "tmp-" + this.getClass().getCanonicalName() + "-" + random.nextInt(10000);
			task2(tmp, tmp2);
			task3(tmp2,cmdline.getOptionValue(REDIR_OPTION),cmdline.getOptionValue(OUTPUT_OPTION));
			}*/

		
		return 0;
	}

	private void task1(String inputPath, String outputPath) throws IOException,
	ClassNotFoundException, InterruptedException {
		LOG.info("Exracting anchor text (phase 1)...");
		LOG.info(" - input: " + inputPath);
		LOG.info(" - output: " + outputPath);

		Job job = Job.getInstance(getConf());
		job.setJarByClass(ExtractWikipediaAnchorTextWithWindow.class);
		job.setJobName(String.format(
				"ExtractWikipediaAnchorText:phase1[input: %s, output: %s]", 
				inputPath, outputPath));

		// 10 reducers is reasonable.
		job.setNumReduceTasks(10);

		// increase heap
		job.getConfiguration().set("mapreduce.job.user.classpath.first", "true");
		
		job.getConfiguration().set("mapreduce.map.memory.mb", "6144");
		job.getConfiguration().set("mapreduce.reduce.memory.mb", "6144");
		job.getConfiguration().set("mapreduce.map.java.opts", "-Xmx6144m");
		job.getConfiguration().set("mapreduce.reduce.java.opts", "-Xmx6144m");
		job.getConfiguration().set("mapreduce.job.user.classpath.first", "true");

		FileInputFormat.setInputPaths(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		job.setInputFormatClass(SequenceFileInputFormat.class);
		// job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setOutputFormatClass(org.apache.hadoop.mapreduce.lib.output.TextOutputFormat.class);

		job.setMapOutputKeyClass(PairOfStringInt.class);
		job.setMapOutputValueClass(PairOfStrings.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(PairOfIntString.class);

		job.setMapperClass(MyMapper1.class);
		job.setReducerClass(MyReducer1.class);
		job.setPartitionerClass(MyPartitioner1.class);

		// Delete the output directory if it exists already.
		FileSystem.get(job.getConfiguration()).delete(new Path(outputPath), true);

		job.waitForCompletion(true);
	}

	private void task2(String inputPath, String outputPath) throws IOException, 
	ClassNotFoundException, InterruptedException {
		LOG.info("Exracting anchor text (phase 2)...");
		LOG.info(" - input: " + inputPath);
		LOG.info(" - output: " + outputPath);

		Job job = Job.getInstance(getConf());
		job.setJarByClass(ExtractWikipediaAnchorTextWithWindow.class);
		job.setJobName(String.format(
				"ExtractWikipediaAnchorText:phase2[input: %s, output: %s]",
				inputPath, outputPath));

		// Gathers everything together for convenience; feasible for Wikipedia.
		job.setNumReduceTasks(1);

		// increase heap
		job.getConfiguration().set("mapreduce.job.user.classpath.first", "true");
		
		job.getConfiguration().set("mapreduce.map.memory.mb", "6144");
		job.getConfiguration().set("mapreduce.reduce.memory.mb", "6144");
		job.getConfiguration().set("mapreduce.map.java.opts", "-Xmx6144m");
		job.getConfiguration().set("mapreduce.reduce.java.opts", "-Xmx6144m");
		job.getConfiguration().set("mapreduce.job.user.classpath.first", "true");
		
		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		job.setInputFormatClass(SequenceFileInputFormat.class);
		job.setOutputFormatClass(MapFileOutputFormat.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(HMapSIW.class);

		job.setMapperClass(MyMapper2.class);
		job.setReducerClass(MyReducer2.class);

		// Delete the output directory if it exists already.
		FileSystem.get(job.getConfiguration()).delete(new Path(outputPath), true);

		job.waitForCompletion(true);

		// Clean up intermediate data.
		FileSystem.get(job.getConfiguration()).delete(new Path(inputPath), true);
	}

	// resolve the redirect
	private void task3(String inputPath, String redirectPath, String outputPath) throws IOException {

		// caches
		IntWritable mapKey = new IntWritable();
		HMapSIW mapVal = new HMapSIW();
		HMapSIW tmpMap = new HMapSIW();
		IntWritable target = new IntWritable(0);

		// read the redirect file
		MapFile.Reader redirectReader = null;
		MapFile.Writer mapWriter = null;
		MapFile.Reader mapReader = null;

		try {
			mapReader = new MapFile.Reader(new Path(inputPath + "/part-r-00000"), getConf());

			redirectReader = new MapFile.Reader(new Path(redirectPath), getConf());

			// TODO: Change code here
			mapWriter = new MapFile.Writer(getConf(), new Path(outputPath), 
					MapFile.Writer.keyClass(IntWritable.class),
					MapFile.Writer.valueClass(HMapSIW.class));

			while(mapReader.next(mapKey, mapVal)) {
				redirectReader.get(mapKey, target);
				if (target.get() > 0) {
					mapReader.get(target, tmpMap);	
					if (!tmpMap.isEmpty()) {
						tmpMap.putAll(mapVal);
						mapWriter.append(target, tmpMap);
					}
				} else {
					mapWriter.append(mapKey, mapVal);
				}
			}
		} finally {
			if (mapWriter != null) mapWriter.close();
			if (mapReader != null) mapReader.close();
			if (redirectReader != null) redirectReader.close();

			// Clean up intermediate data.
			FileSystem.get(getConf()).delete(new Path(inputPath), true);
		}
	}

	public ExtractWikipediaAnchorTextWithWindow() {}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new ExtractWikipediaAnchorTextWithWindow(), args);
		System.exit(res);
	}
}
