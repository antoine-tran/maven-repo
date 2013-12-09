package edu.umd.cloud9.collection.wikipedia;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuan.hadoop.conf.JobConfig;
import tuan.wikipedia.StringUtils;

/** Extract redirect mappings with list of Wikipedia page IDs */
public class DumpIdTitleMap extends JobConfig implements Tool {

	private static final Logger log = 
			LoggerFactory.getLogger(DumpIdTitleMap.class);

	private static final String LANG_OPT = "lang";
	private static final String INPUT_OPT = "in";
	private static final String OUTPUT_OPT = "out";
	private static final String REDUCE_NO = "reduce";

	private static String TMP_HDFS_DIR = "/user/tuan.tran/tmp/";

	/** Preprocess: Extract capitalized wiki page titles / id mappings & output
	 * to a csv file, format: [capitalized title] TAB [id] */
	private static final class MyMapper extends 
	Mapper<LongWritable, WikipediaPage, Text, IntWritable> {

		private Text outKey = new Text();
		private IntWritable outVal = new IntWritable();

		@Override
		protected void map(LongWritable key, WikipediaPage p, Context context) 
				throws IOException, InterruptedException {

			// only articles are emitted
			if (!p.isArticle())
				return;
			String title = p.getTitle().trim();
			int docId = Integer.parseInt(p.getDocid());

			if (title.isEmpty())
				return;

			String nTitle = StringUtils.normalizeWiki(title);
			outKey.set(nTitle);
			outVal.set(docId);
			context.write(outKey, outVal);
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public int run(String[] args) throws Exception {
		Options opts = new Options();

		Option langOpt = OptionBuilder.withArgName("lang").hasArg()
				.withDescription("language of the Wikipedia dump file")
				.create(LANG_OPT);

		Option inputOpt = OptionBuilder.withArgName("input-path").hasArg()
				.withDescription("XML dump file path (required)")
				.create(INPUT_OPT);

		Option outputOpt = OptionBuilder.withArgName("output-path").hasArg()
				.withDescription("output directory path (required)")
				.create(OUTPUT_OPT);


		Option reduceOpt = OptionBuilder.withArgName("reduce-no").hasArg()
				.withDescription("number of reducer nodes").create(REDUCE_NO);


		opts.addOption(langOpt);
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

		String lang = "en";
		if (cl.hasOption(LANG_OPT)) {
			lang = cl.getOptionValue(LANG_OPT);
		}

		String outputPath = TMP_HDFS_DIR + output;

		Job job = setup("Build Wikipedia Id-Title Mapping Graph",
				DumpIdTitleMap.class, 
				input, outputPath, 
				WikipediaPageInputFormat.class, 
				TextOutputFormat.class, 
				Text.class, IntWritable.class, 
				Text.class, IntWritable.class, 
				MyMapper.class,	Reducer.class, 
				reduceNo);

		String ramUsedForEachMapper = job.getConfiguration().get("mapred.map.child.java.opts");		
		log.info("Memory used per Map task: " + ramUsedForEachMapper);

		job.waitForCompletion(true);				
		return 0;
	}

	public static void main(String[] args) {
		try {
			ToolRunner.run(new DumpIdTitleMap(), args);
		} catch (Exception e) {
			log.error("Cannot run: ", e);
		}
	}
}