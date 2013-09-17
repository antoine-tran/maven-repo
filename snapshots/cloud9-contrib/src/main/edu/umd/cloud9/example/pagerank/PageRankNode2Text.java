package edu.umd.cloud9.example.pagerank;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/** This program reads a sequenceFile of PageRankNode objects and returns a single .csv files of [pageId] TAB [pagerank]*/
public class PageRankNode2Text extends Configured implements Tool {

	private static final String INPUT = "input";
	private static final String OUTPUT = "output";

	private static final Logger LOG = Logger.getLogger(PageRankNode2Text.class);


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

		Configuration conf = getConf();
		FileSystem fs = FileSystem.get(conf);

		FileStatus[] statuses = fs.listStatus(new Path(inputPath));
		for (FileStatus s : statuses) {
			if (s.getPath().getName().contains("part-")) {
				SequenceFile.Reader reader = new SequenceFile.Reader(fs, s.getPath(), conf);
				
			}
		}

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
