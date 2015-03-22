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
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuan.hadoop.conf.JobConfig;
import tuan.wikipedia.StringUtils;

/** 
 * Extract redirect mappings with list of Wikipedia page IDs 
 * 
 * @author tuan
 * */
public class DumpIdTitleMap extends JobConfig implements Tool {

	private static final Logger log = 
			LoggerFactory.getLogger(DumpIdTitleMap.class);

	private static final String LANG_OPT = "lang";
	private static final String INPUT_OPT = "in";
	private static final String OUTPUT_OPT = "out";
	private static final String REDUCE_NO = "reduce";
	private static final String NORMALIZE_OPT = "norm";
	private static final String OUTPUT_FORMAT_OPT = "outformat";
	private static final String INPUT_FORMAT_OPT = "informat";
	
	private static final String NORMALIZE = "wikipedia.output.normalized";
	
	private static String TMP_HDFS_DIR = "/user/tuan.tran/tmp/";

	/** Preprocess: Extract capitalized wiki page titles / id mappings & output
	 * to a csv file, format: [capitalized title] TAB [id] */
	private static final class MyMapper extends 
	Mapper<LongWritable, WikipediaPage, Text, LongWritable> {

		private Text outKey = new Text();
		private LongWritable outVal = new LongWritable();

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

			boolean normalized = context.getConfiguration().getBoolean(NORMALIZE, false);
						
			String nTitle = (normalized) ? StringUtils.normalizeWiki(title) : title;
			outKey.set(nTitle);
			outVal.set(docId);
			context.write(outKey, outVal);
		}
	}
	
	// Only emit one of them
	private static final class MyReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
		
		private LongWritable outval = new LongWritable();
		@Override
		protected void reduce(Text k, Iterable<LongWritable> vs, Context c)
				throws IOException, InterruptedException {
			for (LongWritable v : vs) outval.set(v.get());
			c.write(k,outval);
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
				.withDescription("binary-compacked dump file path (required)")
				.create(INPUT_OPT);

		Option outputOpt = OptionBuilder.withArgName("output-path").hasArg()
				.withDescription("output directory path (required)")
				.create(OUTPUT_OPT);

		Option reduceOpt = OptionBuilder.withArgName("reduce-no").hasArg()
				.withDescription("number of reducer nodes").create(REDUCE_NO);
		
		Option normalizeOpt = OptionBuilder.withArgName("normalization").hasArg(false)
				.withDescription("normalize is needed ?").create(NORMALIZE_OPT);
		
		Option outputFormatOpt = OptionBuilder.withArgName("output format").hasArg()
				.withDescription("Output format: Text of sequential").create(OUTPUT_FORMAT_OPT);
		
		Option inputFormatOpt = OptionBuilder.withArgName("input format").hasArg()
				.withDescription("Output format: Text of sequential").create(INPUT_FORMAT_OPT);

		opts.addOption(langOpt);
		opts.addOption(inputOpt);
		opts.addOption(reduceOpt);
		opts.addOption(outputOpt);
		opts.addOption(normalizeOpt);
		opts.addOption(outputFormatOpt);
		opts.addOption(inputFormatOpt);
 
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

		boolean normalized = cl.hasOption(NORMALIZE_OPT);		
		
		String outputFormat = "text";
		if (cl.hasOption(OUTPUT_FORMAT_OPT)) {
			outputFormat = cl.getOptionValue(OUTPUT_FORMAT_OPT);
		}
		
		String inputFormat = "binary";
		if (cl.hasOption(INPUT_FORMAT_OPT)) {
			inputFormat = cl.getOptionValue(INPUT_FORMAT_OPT);
		}
		
		Job job = setup("Build Wikipedia Id-Title Mapping Graph",
				DumpIdTitleMap.class, 
				input, outputPath, 
				inputFormat.equals("binary") ? SequenceFileInputFormat.class :
				WikipediaPageInputFormat.class,
				
				// Uncomment this if want output to be a text file
				outputFormat.equals("text") ? TextOutputFormat.class :
				
				// Uncomment this if want output to be a binary file
				SequenceFileOutputFormat.class,
				
				Text.class, LongWritable.class, 
				Text.class, LongWritable.class, 
				MyMapper.class,	Reducer.class, Reducer.class, 
				reduceNo);

		String ramUsedForEachMapper = job.getConfiguration().get("mapred.map.child.java.opts");		
		log.info("Memory used per Map task: " + ramUsedForEachMapper);

		job.getConfiguration().setBoolean(NORMALIZE, normalized);
		
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