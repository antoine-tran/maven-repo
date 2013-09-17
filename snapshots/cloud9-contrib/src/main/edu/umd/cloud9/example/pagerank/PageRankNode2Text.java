package edu.umd.cloud9.example.pagerank;

import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import tuan.hadoop.conf.JobConfig;

/** This program reads a sequenceFile of PageRankNode objects and returns a single .csv files of [pageId] TAB [pagerank]*/
public class PageRankNode2Text extends JobConfig implements Tool {

	private static final String INPUT = "input";
	private static final String OUTPUT = "output";

	private static final Logger LOG = Logger.getLogger(PageRankNode2Text.class);

	private static final class PageRankNodeResolver extends Mapper<IntWritable, PageRankNode, IntWritable, Text> {

		Text val = new Text();
		
		@Override
		protected void map(IntWritable key, PageRankNode n, Context context)
				throws IOException, InterruptedException {			
			String str = String.format("%d\t%.4f", n.getNodeId(), n.getPageRank());
			val.set(str);
			context.write(key, val);
		}		
	}

	@SuppressWarnings("static-access")
	@Override
	public int run(String[] args) throws Exception {
		Options options = new Options();

		options.addOption(OptionBuilder.withArgName("path").hasArg()
				.withDescription("input path").create(INPUT));
		options.addOption(OptionBuilder.withArgName("path").hasArg()
				.withDescription("output path").create(OUTPUT));

		CommandLine cmdline = null;
		CommandLineParser parser = new GnuParser();

		try {
			cmdline = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("Error parsing command line: " + exp.getMessage());
			System.exit(-1);
		}

		if (!cmdline.hasOption(INPUT) || !cmdline.hasOption(OUTPUT)) {
			System.out.println("args: " + Arrays.toString(args));
			HelpFormatter formatter = new HelpFormatter();
			formatter.setWidth(120);
			formatter.printHelp(PageRankNode2Text.class.getName(), options);
			ToolRunner.printGenericCommandUsage(System.out);
			System.exit(-1);
		}

		String inputPath = cmdline.getOptionValue(INPUT);
		String outputPath = cmdline.getOptionValue(OUTPUT);

		LOG.info("Tool name: " + PageRankNode2Text.class.getSimpleName());
		LOG.info(" - input: " + inputPath);
		LOG.info(" - output: " + outputPath);

		Job job = setup("Debug PageRank intermediate results with csv format",
				PageRankNode2Text.class, 
				inputPath, outputPath, 
				SequenceFileInputFormat.class, TextOutputFormat.class, 
				IntWritable.class, Text.class, 
				IntWritable.class, Text.class,
				PageRankNodeResolver.class, Reducer.class, 1);

		job.setCombinerClass(Reducer.class);
		job.waitForCompletion(true);
		return 0;
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		try {
			ToolRunner.run(new PageRankNode2Text(), args);
		} catch (Exception e) {
			LOG.error("FATAL EXCEPTION: ", e);
		}
	}
}
