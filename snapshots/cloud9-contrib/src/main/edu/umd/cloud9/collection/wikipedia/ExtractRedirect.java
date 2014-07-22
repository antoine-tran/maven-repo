package edu.umd.cloud9.collection.wikipedia;


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
import org.apache.commons.lang.WordUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cloud9.io.pair.PairOfInts;
import edu.umd.cloud9.mapreduce.StructureMessageResolver;
import tuan.hadoop.conf.JobConfig;

/** Extract redirect mappings with list of Wikipedia page IDs */
public class ExtractRedirect extends JobConfig implements Tool {

	private static final Logger log = 
			LoggerFactory.getLogger(ExtractRedirect.class);

	private static final String LANG_OPT = "lang";
	private static final String INPUT_OPT = "in";
	private static final String OUTPUT_OPT = "out";
	private static final String REDUCE_NO = "reduce";
	private static final String OUTPUT_FORMAT_OPT = "out_format";

	private static String TMP_HDFS_DIR = "/user/tuan.tran/tmp/";

	/** Preprocess: Extract capitalized wiki page titles / id mappings & output
	 * to a csv file, format: [capitalized title] TAB [id] */
	private static final class MyMapper extends 
	Mapper<IntWritable, WikipediaPage, Text, PairOfInts> {

		private Text outKey = new Text();
		private PairOfInts outVal = new PairOfInts();

		@Override
		protected void map(IntWritable key, WikipediaPage p, Context context) 
				throws IOException, InterruptedException {

			log.debug("Processing page: " + p.getDocid());

			// only articles are emitted
			boolean redirected = false;
			if (p.isRedirect()) {
				redirected = true;
			} else if (!p.isArticle())
				return;
			String title = p.getTitle().trim();
			int docId = Integer.parseInt(p.getDocid());

			// to make the title case-sensitive, we will change all lower-cased
			// first characters to upper-case.
			if (title.isEmpty())
				return;
			title = WordUtils.capitalize(title);

			// do not pass the id message of a redirect article
			if (!redirected) {
				outKey.set(title);
				outVal.set(docId, -1);
				context.write(outKey, outVal);
			} else {
				String actualTitle = null;
				for (String t : p.extractLinkTargets()) {
					if (t.isEmpty()) {
						continue;
					}
					actualTitle = WordUtils.capitalize(t);
				}	
				if (actualTitle != null) {
					outKey.set(actualTitle);
					outVal.set(docId, 1);
					log.debug("Found redirect: " + title + " --> " + actualTitle);
					context.write(outKey, outVal);
				}
			}
		}
	}

	/** Resolve redirects and output (title, id) pairs */
	private static final class RedirectResolver extends 
	StructureMessageResolver<Text, PairOfInts, IntWritable, IntWritable> {

		private List<PairOfInts> cache;

		private IntWritable keyOut = new IntWritable();
		private IntWritable valOut = new IntWritable();

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			cache = new ArrayList<PairOfInts>();
		}

		@Override
		public void setupTask(Text key, Iterable<PairOfInts> values, Context context) {
			cache.clear();
		}

		@Override
		public boolean checkStructureMessage(Text key, PairOfInts msg, Context c) 
				throws IOException, InterruptedException {
			int i = msg.getValue();
			if (i == -1) {
				valOut.set(msg.getKey());
				return true;
			} else return false;
		}

		@Override
		public PairOfInts clone(PairOfInts obj) {
			PairOfInts newObj = new PairOfInts(obj.getKey(), obj.getValue());
			return newObj;
		}

		@Override
		public void messageAfterHits(Context context, Text key, PairOfInts structureMsg,
				PairOfInts msg) throws IOException, InterruptedException {
			keyOut.set(msg.getKey());
			valOut.set(structureMsg.getKey());
			context.write(keyOut, valOut);
		}

		@Override
		public void flushWithoutHit(Context context) throws IOException, InterruptedException {
			log.warn("No id found for page " + keyOut.toString());
		}

		@Override
		public void messageBeforeHits(Text key, PairOfInts v) {
			cache.add(new PairOfInts(v.getKey(), v.getValue()));
		}

		@Override
		public Iterable<PairOfInts> tempValuesCache() {
			return cache;
		}
	}

	private void phase1(String wikiFile, int reduceNo, String lang, String output, String format) 
			throws IOException, InterruptedException, ClassNotFoundException {

		// String outputPath = TMP_HDFS_DIR + output;
		String tmp = TMP_HDFS_DIR + output;
		
		Job job = null;
		job = setup("Build Wikipedia Redirect Mapping Graph - processing",
				ExtractAnchor.class, 
				wikiFile, tmp, 
				SequenceFileInputFormat.class, 
				SequenceFileOutputFormat.class, 
				Text.class, PairOfInts.class, 
				IntWritable.class, IntWritable.class, 
				MyMapper.class, 
				RedirectResolver.class, 
				reduceNo);

		String ramUsedForEachMapper = job.getConfiguration().get("mapred.map.child.java.opts");		
		log.info("Memory used per Map task: " + ramUsedForEachMapper);
		job.waitForCompletion(true);	
		
		log.info("Phase 2..");
		if ("text".equals(format)) {
			job = setup("Build Wikipedia Redirect Mapping Graph - sorting",
					ExtractAnchor.class, 
					tmp, output, 
					SequenceFileInputFormat.class, 
					TextOutputFormat.class, 
					IntWritable.class, IntWritable.class, 
					IntWritable.class, IntWritable.class, 
					Mapper.class, Reducer.class, 1);
		} else if ("map".equals(format)) {
			job = setup("Build Wikipedia Redirect Mapping Graph - sorting",
					ExtractAnchor.class, 
					tmp, output, 
					SequenceFileInputFormat.class, 
					MapFileOutputFormat.class, 
					IntWritable.class, IntWritable.class, 
					IntWritable.class, IntWritable.class, 
					Mapper.class, Reducer.class, 1);
		} else throw new RuntimeException("unknown output format: " + format);
		job.waitForCompletion(true);
	
		// remove intermediate file
		FileSystem.get(job.getConfiguration()).delete(new Path(tmp), true);
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

	public static void main(String[] args) {
		try {
			ToolRunner.run(new ExtractRedirect(), args);
		} catch (Exception e) {
			log.error("Cannot run: ", e);
		}
	}
}
