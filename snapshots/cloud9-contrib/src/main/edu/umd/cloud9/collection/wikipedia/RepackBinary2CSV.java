package edu.umd.cloud9.collection.wikipedia;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import tuan.hadoop.conf.JobConfig;

public class RepackBinary2CSV extends JobConfig implements Tool {

	private static final String INPUT_OPT = "in";
	private static final String OUTPUT_OPT = "out";
	private static final String REDUCE_NO = "reduce";
	
	@SuppressWarnings("static-access")
	@Override
	public int run(String[] args) throws Exception {
		Options opts = new Options();


		Option inputOpt = OptionBuilder.withArgName("input-path").hasArg()
				.withDescription("binary-compacked dump file path (required)")
				.create(INPUT_OPT);

		Option outputOpt = OptionBuilder.withArgName("output-path").hasArg()
				.withDescription("output directory path (required)")
				.create(OUTPUT_OPT);


		Option reduceOpt = OptionBuilder.withArgName("reduce-no").hasArg()
				.withDescription("number of reducer nodes").create(REDUCE_NO);

		opts.addOption(inputOpt);
		opts.addOption(reduceOpt);
		opts.addOption(outputOpt);
 
		CommandLine cl;
		CommandLineParser parser = new GnuParser();
		try {
			cl = parser.parse(opts, args);
		} catch (ParseException e) {
			System.err.println("Error parsing command line: " + e.getMessage());
			return -1;
		}
		if (!cl.hasOption(INPUT_OPT) || !cl.hasOption(OUTPUT_OPT)) {
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
				System.err.println("Error parsing reducer number: "
						+ e.getMessage());
			}
		}

		String input = cl.getOptionValue(INPUT_OPT);
		String output = cl.getOptionValue(OUTPUT_OPT);
		
		Job job = setup("Repack string/int binary file into csv file",
				RepackBinary2CSV.class, 
				input, output, 
				SequenceFileInputFormat.class, 
				
				// Uncomment this if want output to be a text file
				TextOutputFormat.class,
				
				// Uncomment this if want output to be a binary file
				// SequenceFileOutputFormat.class,
				
				Text.class, IntWritable.class, 
				Text.class, IntWritable.class, 
				Mapper.class,	Reducer.class, 
				reduceNo);
		
		job.waitForCompletion(true);
		
		return 0;
	}

	public static void main(String[] args) {
		try {
			ToolRunner.run(new RepackBinary2CSV(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
