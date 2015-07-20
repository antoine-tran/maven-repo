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

public class SimpleRedirect extends JobConfig implements Tool {

	private static final Logger log = 
			LoggerFactory.getLogger(SimpleRedirect.class);
	
	private static final String LANG_OPT = "lang";
	private static final String INPUT_OPT = "in";
	private static final String OUTPUT_OPT = "out";
	private static final String REDUCE_NO = "reduce";
	private static final String OUTPUT_FORMAT_OPT = "out_format";
	
	private static String TMP_HDFS_DIR = "/user/tuan.tran/tmp/";	
	
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
				.withDescription("output file path (required)")
				.create(OUTPUT_OPT);

		Option outputFormatOpt = OptionBuilder.withArgName("output-format").hasArg()
				.withDescription("output file path (required)")
				.create(OUTPUT_FORMAT_OPT);

		Option reduceOpt = OptionBuilder.withArgName("reduce-no").hasArg()
				.withDescription("number of reducer nodes").create(REDUCE_NO);


		opts.addOption(langOpt);
		opts.addOption(inputOpt);
		opts.addOption(reduceOpt);
		opts.addOption(outputOpt);
		opts.addOption(outputFormatOpt);

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
		String outputFm = cl.getOptionValue(OUTPUT_FORMAT_OPT);
		if (outputFm == null)
			outputFm = "text";

		String lang = "en";
		if (cl.hasOption(LANG_OPT)) {
			lang = cl.getOptionValue(LANG_OPT);
		}

		phase1(input, reduceNo, lang, output, outputFm);
		return 0;
	}
	
	/** Preprocess: Extract capitalized wiki page titles / id mappings & output
	 * to a csv file, format: [capitalized title] TAB [id] */
	private static final class MyMapper extends 
	Mapper<LongWritable, WikipediaPage, LongWritable, Text> {
		
		private final LongWritable KEY = new LongWritable();
		private final Text VALUE = new Text();
		@Override
		protected void map(LongWritable key, WikipediaPage p,
				Context context) throws IOException, InterruptedException {
			log.debug("Processing page: " + p.getDocid());

			// only articles are emitted
			boolean redirected = false;
			if (p.isRedirect()) {
				redirected = true;
			} else if (!p.isArticle())
				return;
			String title = p.getTitle().trim();

			String fc = title.substring(0, 1);
			if (fc.matches("[A-Z]")) {
				title = title.replaceFirst(fc, fc.toUpperCase());
			}

			long docId = Long.parseLong(p.getDocid());

			// to make the title case-sensitive, we will change all lower-cased
			// first characters to upper-case.
			if (title.isEmpty())
				return;
			// title = WordUtils.capitalize(title);

			// do not pass the id message of a redirect article
			if (!redirected) {
				KEY.set(docId);
				VALUE.set(title + "\t-1");
				context.write(KEY, VALUE);

				// } else {	
				//} else if (p.isDisambiguation()) {
			} else if (p.isRedirect()) {
				String actualTitle = null;
				for (String t : p.extractLinkTargets()) {
					if (t.isEmpty()) {
						continue;
					}
					fc = t.substring(0, 1);
					if (fc.matches("[A-Z]")) {
						actualTitle = title.replaceFirst(fc, fc.toUpperCase());
					}
					else {
						actualTitle = title;
					}
				}	
				if (actualTitle != null) {
					KEY.set(docId);
					VALUE.set(actualTitle + "\t1");
					context.write(KEY, VALUE);
				}
			}
		}
	}
	
	private void phase1(String wikiFile, int reduceNo, String lang, String output, String format) 
			throws IOException, InterruptedException, ClassNotFoundException {

		String tmp = TMP_HDFS_DIR + output;

		Job job = null;
		job = setup("Build Wikipedia Redirect Mapping Graph - processing",
				SimpleRedirect.class, 
				wikiFile, tmp, 
				WikipediaPageInputFormat.class, 
				TextOutputFormat.class, 
				LongWritable.class, Text.class, 
				LongWritable.class, Text.class, 
				MyMapper.class, 
				Reducer.class, 
				reduceNo);

		String ramUsedForEachMapper = job.getConfiguration().get("mapred.map.child.java.opts");		
		log.info("Memory used per Map task: " + ramUsedForEachMapper);
		job.waitForCompletion(true);
	}

	public static void main(String[] args) {
		try {
			ToolRunner.run(new SimpleRedirect(), args);
		} catch (Exception e) {
			log.error("Cannot run: ", e);
		}
	}

}
