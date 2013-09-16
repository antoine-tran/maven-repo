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
import org.apache.commons.lang.WordUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.reduce.IntSumReducer;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cloud9.collection.wikipedia.WikipediaPage.Link;
import edu.umd.cloud9.io.pair.PairOfStringInt;
import edu.umd.cloud9.mapreduce.StructureMessageResolver;

import tuan.hadoop.conf.JobConfig;
import tuan.io.FileUtility;

/** Extract anchor texts together with its list of Wikipedia page IDs and count
 *  of references to the page using the anchors. It resolves the redirects, i.e.
 *  title of a redirect page will be mapped to the id of the actual page.
 *  This program is to replace the  BuildAnchorText, which uses the cumbersome 
 *  StructureMessageResolver */
public class ExtractAnchor extends JobConfig implements Tool {

	private static final Logger log = 
			LoggerFactory.getLogger(ExtractAnchor.class);
	
	private static final String LANG_OPT = "lang";
	private static final String INPUT_OPT = "in";
	private static final String OUTPUT_OPT = "out";
	private static final String REDUCE_NO = "reduce";
	private static final String PHASE = "phase";
	private static final String TITLE_ID_MAP_OPT = "idmap";
	private static final String TMP_DIR_OPT = "tmpdir";
	
	private static String TMP_HDFS_DIR = "/user/tuan.tran/tmp/";
	
    /** Preprocess: Extract capitalized wiki page titles / id mappings & output
     * to a csv file, format: [capitalized title] TAB [id] */
	private static final class TitleIdMapper extends 
			Mapper<LongWritable, WikipediaPage, Text, PairOfStringInt> {

		private Text outKey = new Text();
		private PairOfStringInt outVal = new PairOfStringInt();
		
		@Override
		protected void map(LongWritable key, WikipediaPage p, Context context) 
				throws IOException, InterruptedException {
			
			log.debug("Processing page: " + p.getDocid());

			// only articles are emitted
			boolean redirected = false;
			if (p.isRedirect()) {
				redirected = true;
			} else if (!p.isArticle())
				return;
			String title = p.getTitle().trim();

			// to make the title case-sensitive, we will change all lower-cased
			// first characters to upper-case.
			if (title.isEmpty())
				return;
			title = WordUtils.capitalize(title);

			// do not pass the id message of a redirect article
			if (!redirected) {
				outKey.set(title);
				outVal.set(p.getDocid(), -1);
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
					outVal.set(title, 1);
					log.debug("Found redirect: " + title + " --> " + actualTitle);
					context.write(outKey, outVal);
				}
			}
		}
	}
	
	/** Resolve redirects and output (title, id) pairs */
	private static final class RedirectResolver extends 
			StructureMessageResolver<Text, PairOfStringInt, Text, IntWritable> {
		
		private List<PairOfStringInt> cache;
		
		private Text keyOut = new Text();
		private IntWritable valOut = new IntWritable();

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			cache = new ArrayList<PairOfStringInt>();
		}

		@Override
		public void setupTask(Text key, Iterable<PairOfStringInt> values, Context context) {
			cache.clear();
			keyOut.set(key.toString());
			valOut.set(0);
		}
		
		@Override
		public boolean checkStructureMessage(Text key, PairOfStringInt msg, Context c) 
				throws IOException, InterruptedException {
			int i = msg.getValue();
			if (i == -1) {
				valOut.set(Integer.parseInt(msg.getKey()));
				c.write(keyOut, valOut);
				return true;
			} else return false;
		}
		
		@Override
		public PairOfStringInt clone(PairOfStringInt obj) {
			PairOfStringInt newObj = new PairOfStringInt(obj.getKey(), obj.getValue());
			return newObj;
		}

		@Override
		public void messageAfterHits(Context context, Text key, PairOfStringInt structureMsg,
				PairOfStringInt msg) throws IOException, InterruptedException {
			keyOut.set(msg.getKey());
			valOut.set(Integer.parseInt(structureMsg.getKey()));
			context.write(keyOut, valOut);
		}

		@Override
		public void flushWithoutHit(Context context) throws IOException, InterruptedException {
			log.warn("No id found for page " + keyOut.toString());
		}

		@Override
		public void messageBeforeHits(Text key, PairOfStringInt v) {
			cache.add(new PairOfStringInt(v.getKey(), v.getValue()));
		}

		@Override
		public Iterable<PairOfStringInt> tempValuesCache() {
			return cache;
		}
	}

	/** Extract anchors */
	private static final class AnchorMapper extends
			Mapper<LongWritable, WikipediaPage, PairOfStringInt, IntWritable> {
		
		private Object2IntOpenHashMap<String> titleId;
		
		private PairOfStringInt KEY = new PairOfStringInt();
		private IntWritable ONE = new IntWritable(1);
		
		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			String path = context.getConfiguration().get("wiki.id.cache");
			FileSystem fs = FileSystem.get(context.getConfiguration());
			Path p = new Path(path);
			Configuration c = context.getConfiguration();
			loadIdTitleMap(p, c, fs);
		}
		
		private void loadIdTitleMap(Path p, Configuration c, FileSystem fs) throws IOException {
			if (titleId == null) titleId = new Object2IntOpenHashMap<String>();
			SequenceFile.Reader reader = null;
			try {
				reader = new SequenceFile.Reader(fs, p, c);
				Writable key = (Writable) ReflectionUtils.newInstance(reader.getKeyClass(), c);
				Writable val = (Writable) ReflectionUtils.newInstance(reader.getValueClass(), c);
				while (reader.next(key, val)) {
					String title = ((Text)key).toString();
					int id = ((IntWritable)val).get();
					titleId.addTo(title, id);
				}
			} finally {
				IOUtils.closeStream(reader);
			}
		}

		@Override
		protected void map(LongWritable key, WikipediaPage p, Context context)
				throws IOException, InterruptedException {
			
			if (p.isRedirect() || !p.isArticle()) return;
			
			/*String pageId = p.getDocid();
			int id = Integer.parseInt(pageId);*/
						
			for (Link link : p.extractLinks()) {
				String anchor = link.getAnchorText();
				String target = link.getTarget();
				target = WordUtils.capitalize(target);
				if (titleId.containsKey(target)) {
					int id = titleId.getInt(target);
					KEY.set(anchor, id);
					context.write(KEY, ONE);
				}
			}
		}
	}
	
	private void phase1(String wikiFile, int reduceNo, String lang, String mapPath) throws IOException, InterruptedException, ClassNotFoundException {
		
		String outputPath = TMP_HDFS_DIR + mapPath;
		
		Job job = setup("Build Wikipedia Anchor Text Graph. Phase 1: Resolving redirects",
				ExtractAnchor.class, 
				wikiFile, outputPath, 
				WikipediaPageInputFormat.class, 
				SequenceFileOutputFormat.class, 
				Text.class, PairOfStringInt.class, 
				Text.class, IntWritable.class, 
				TitleIdMapper.class, 
				RedirectResolver.class, 
				reduceNo);
		
		String ramUsedForEachMapper = job.getConfiguration().get("mapred.map.child.java.opts");		
		log.info("Memory used per Map task: " + ramUsedForEachMapper);
		
		job.waitForCompletion(true);		
	}
	
	private void phase2(String wikiMap, String wikiFile, String output, int reduceNo) throws IOException, InterruptedException, ClassNotFoundException {
		String wikiMapPath = TMP_HDFS_DIR + wikiMap + "/part-r-00000";
		String outputPath = TMP_HDFS_DIR + output;
		
		Job job = setup("Build Wikipedia Anchor Text Graph. Phase 2: Extracting anchors",
				ExtractAnchor.class, 
				wikiFile, outputPath, 
				WikipediaPageInputFormat.class, 
				TextOutputFormat.class, 
				PairOfStringInt.class, IntWritable.class, 
				PairOfStringInt.class, IntWritable.class, 
				AnchorMapper.class, 
				IntSumReducer.class, 
				reduceNo);
		job.getConfiguration().set("mapred.map.child.java.opts", "-Xmx5120M");
		job.getConfiguration().set("wiki.id.cache", wikiMapPath);
		job.setCombinerClass(IntSumReducer.class);
		job.waitForCompletion(true);
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
		
		Option tmpOpt = OptionBuilder.withArgName("tmp-directory").hasArg()
				.withDescription("temporary directory in HDFS")
				.create(TMP_DIR_OPT);

		Option reduceOpt = OptionBuilder.withArgName("reduce-no").hasArg()
				.withDescription("number of reducer nodes").create(REDUCE_NO);

		Option phaseOpt = OptionBuilder.withArgName("phase-no").hasArg()
				.withDescription("Phase 1: Redirect resolve, Phase 2: Extract Anchor text").create(PHASE);
		
		Option phase1Out = OptionBuilder.withArgName("phase1-out").hasArg()
				.withDescription("(Output) path of title / id map file (required)")
				.create(TITLE_ID_MAP_OPT);

		opts.addOption(langOpt);
		opts.addOption(inputOpt);
		opts.addOption(reduceOpt);
		opts.addOption(phaseOpt);
		opts.addOption(outputOpt);
		opts.addOption(phase1Out);
		opts.addOption(tmpOpt);

		CommandLine cl;
		CommandLineParser parser = new GnuParser();
		try {
			cl = parser.parse(opts, args);
		} catch (ParseException e) {
			System.err.println("Error parsing command line: " + e.getMessage());
			return -1;
		}
		if (!cl.hasOption(INPUT_OPT) || !cl.hasOption(OUTPUT_OPT) || 
				!cl.hasOption(TITLE_ID_MAP_OPT)) {
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
		
		String mapPath = cl.getOptionValue(TITLE_ID_MAP_OPT);
		String input = cl.getOptionValue(INPUT_OPT);
		String output = cl.getOptionValue(OUTPUT_OPT);
		
		int phase = 1;
		if (cl.hasOption(PHASE)) {
			try {
				phase = Integer.parseInt(cl.getOptionValue(PHASE));
			} catch (NumberFormatException e) {
				System.err.println("Error parsing phase number: "
						+ e.getMessage());
			}
		}
		String lang = "en";
		if (cl.hasOption(LANG_OPT)) {
			lang = cl.getOptionValue(LANG_OPT);
		}
		
		if (cl.hasOption(TMP_DIR_OPT)) {
			TMP_HDFS_DIR = cl.getOptionValue(TMP_DIR_OPT);
		}

		if (phase == 1) {
			phase1(input, reduceNo, lang, mapPath);
			log.info("Map written to " + TMP_HDFS_DIR + mapPath);
		} else if (phase == 12) {
			phase1(input, reduceNo, lang, mapPath);
			phase2(mapPath, input, output, reduceNo);
			log.info("Write results to " + output);
		} else if (phase == 2) {
			phase2(mapPath, input, output, reduceNo);
			log.info("Write results to " + output);
		}
		return 0;
	}
	
	public static void main(String[] args) {
		try {
			ToolRunner.run(new ExtractAnchor(), args);
		} catch (Exception e) {
			log.error("Cannot run: ", e);
		}
	}
}
