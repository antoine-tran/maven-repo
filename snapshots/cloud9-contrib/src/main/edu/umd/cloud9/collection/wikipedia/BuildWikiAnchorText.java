package edu.umd.cloud9.collection.wikipedia;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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

import edu.umd.cloud9.io.pair.Pair;
import edu.umd.cloud9.io.pair.PairOfStringInt;
import edu.umd.cloud9.io.pair.PairOfStrings;
import edu.umd.cloud9.mapreduce.StructureMessageResolver;

/**
 * A Hadoop job that extracts anchor text from Wikipedia dump
 * 
 * @author tuan
 * @deprecated use ExtractAnchor instead
 */
@Deprecated
public class BuildWikiAnchorText extends JobConfig implements Tool {

	private static final Logger log = LoggerFactory
			.getLogger(BuildWikiAnchorText.class);

	private static final String LANG_OPTION = "lang";
	private static final String INPUT_OPTION = "in";
	private static final String OUTPUT_OPTION = "out";
	private static final String REDUCE_NO = "reduce";
	private static final String PHASE = "phase";
	private static final String TMP_OUTPUT_OPTION = "tmpout";
	private static final String PHASE1_OPTION = "phase1out";
	
	/**
	 * Map phase 1: Parse one single Wikipedia page and emits, for each outgoing
	 * links in the text, a (destinationLink, anchor) pair
	 */
	private static final class EmitAnchorMapper extends
			Mapper<LongWritable, WikipediaPage, Text, PairOfStringInt> {

		private Text outKey = new Text();
		private PairOfStringInt outVal = new PairOfStringInt();
		private Object2IntOpenHashMap<PairOfStrings> map = new Object2IntOpenHashMap<PairOfStrings>();

		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			super.setup(context);
			map.clear();
		}

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

			// do not pass the structure message of a redirect article
			if (!redirected) {
				outKey.set(title);
				int id = Integer.parseInt(p.getDocid());
				outVal.set(title, id);
				context.write(outKey, outVal);
			}

			for (PairOfStrings t : extractAnchoredLinks(p)) {
				String link = t.getLeftElement().trim();
				if (link.isEmpty())
					continue;
				link = WordUtils.capitalize(link);
				if (title.equals(link))
					continue;
				if (redirected) {
					outKey.set(title);
					outVal.set(link, -1);
					context.write(outKey, outVal);
					return;
				} else
					map.addTo(t, 1);
			}
			PairOfStrings[] keys = map.keySet().toArray(
					(new PairOfStrings[map.size()]));

			for (PairOfStrings k : keys) {
				if (k.getLeftElement().isEmpty())
					continue;
				outKey.set(k.getLeftElement());
				int cnt = map.get(k);
				outVal.set(k.getRightElement(), cnt);
				context.write(outKey, outVal);
			}
		}
	}

	/** Reduce phase 1: Resolve the redirect links */
	private static final class RedirectResolveReducer
			extends StructureMessageResolver<Text, PairOfStringInt, Text, PairOfStringInt> {

		private List<PairOfStringInt> cache = new ArrayList<PairOfStringInt>();

		private Text keyOut = new Text();
		private PairOfStringInt valOut = new PairOfStringInt();
		
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			super.setup(context);
			cache.clear();
		}

		@Override
		// Update the outkey on-the-fly
		public boolean checkStructureMessage(Text key, PairOfStringInt msg, Context context) {
			int v = msg.getValue();
			boolean redirected = (v == -1);
			if (redirected)
				keyOut.set(msg.getKey());
			else
				keyOut.set(key);
			return redirected;
		}

		public PairOfStringInt clone(PairOfStringInt t) {
			return new PairOfStringInt(t.getKey(), t.getValue());
		}


		@Override
		public void messageAfterHits(Context context, Text key,
				PairOfStringInt structureMsg, PairOfStringInt msg)
				throws IOException, InterruptedException {

			// There might be still redirect pages that emit their pageIds.
			// Ignore those
			if ((key.toString().equals(msg.getKey())))
				return;

			// no need to update the out key - we did it in
			// checkStructureMessage() already
			else
				context.write(keyOut, msg);
		}

		@Override
		// The destination page is not a redirect. Emit everything to the phase
		// 2
		public void flushWithoutHit(Context context) throws IOException, InterruptedException {
			if (cache != null && !cache.isEmpty()) {
				for (PairOfStringInt v : cache)
					context.write(keyOut, v);
			} else
				log.debug("No structure message found: " + keyOut.toString()
						+ ", and " + "no values emitted either.");
		}

		@Override
		public Iterable<PairOfStringInt> tempValuesCache() {
			return cache;
		}

		@Override
		public void messageBeforeHits(Text key, PairOfStringInt value) {
			cache.add(clone(value));
		}

		@Override
		public void setupTask(Text key, Iterable<PairOfStringInt> values, Context context) {
			cache.clear();
		}
	}

	private static final class PageIdResolveReducer extends
			StructureMessageResolver<Text, PairOfStringInt, Text, Text> {

		private Object2IntOpenHashMap<String> cache = new Object2IntOpenHashMap<String>();
		
		private Text keyOut = new Text();
		private Text valOut = new Text();

		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			super.setup(context);
			cache.clear();
		}

		@Override
		// Update the output key on-the-fly
		public boolean checkStructureMessage(Text key, PairOfStringInt msg, Context c)
				throws IOException, InterruptedException {
			String dest = key.toString();
			String source = msg.getKey();
			boolean redirected = (dest.equals(source));
			if (redirected)
				keyOut.set(String.valueOf(msg.getValue()));
			else
				keyOut.set(key);
			return redirected;
		}

		public PairOfStringInt clone(PairOfStringInt t) {
			return new PairOfStringInt(t.getKey(), t.getValue());
		}

		@Override
		public void messageAfterHits(Context context, Text key,
				PairOfStringInt structureMsg, PairOfStringInt msg) throws IOException,
				InterruptedException {
			valOut.set(msg.getKey() + "\t" + msg.getValue());
			context.write(keyOut, valOut);
		}

		@Override
		// We lost the structure message of this page. Report it !
		public void flushWithoutHit(Context context) throws IOException, InterruptedException {
			log.info("No structure message found for : " + keyOut.toString());
		}

		@Override
		public void messageBeforeHits(Text key, PairOfStringInt value) {
			cache.addTo(value.getKey(), value.getValue());
		}

		@Override
		public Iterable<PairOfStringInt> tempValuesCache() {
			return new Iterable<PairOfStringInt>() {

				@Override
				public Iterator<PairOfStringInt> iterator() {
					return new Iterator<PairOfStringInt>() {
						ObjectIterator<String> iter = cache.keySet().iterator();

						@Override
						public boolean hasNext() {
							return iter.hasNext();
						}

						@Override
						public PairOfStringInt next() {
							String k = iter.next();
							int v = cache.getInt(k);
							return new PairOfStringInt(k, v);
						}

						@Override
						// Optional, no need to implement
						public void remove() {
						}
					};
				}
			};
		}

		@Override
		public void setupTask(Text key, Iterable<PairOfStringInt> values, Context context) {
			cache.clear();			
		}
	}

	private String phase1(String inputPath, int reduceNo, String lang,
			String tmpOut) throws IOException, InterruptedException,
			ClassNotFoundException {

		String output = tmpOut + "/wiki-anchor/phase1";

		if (!"en".equals(lang))
			throw new InterruptedException("Wikipedia dump with language "
					+ lang + " is not supported ");

		Job job = setup("Build Wikipedia Anchor text graph. Phase 1",
				BuildWikiAnchorText.class, inputPath, output,
				WikipediaPageInputFormat.class, SequenceFileOutputFormat.class,
				Text.class, PairOfStringInt.class, Text.class,
				PairOfStringInt.class, EmitAnchorMapper.class,
				RedirectResolveReducer.class, reduceNo);
		job.getConfiguration().set("mapred.map.child.java.opts", "-Xmx9192M");
		job.setCombinerClass(Reducer.class);
		job.waitForCompletion(true);
		return output;
	}

	private String phase2(String inputPath, String output, int reduceNo)
			throws IOException, InterruptedException, ClassNotFoundException {

		Job job = setup("Build Wikipedia Anchor text graph. Phase 2",
				BuildWikiAnchorText.class, inputPath, output,
				SequenceFileInputFormat.class, TextOutputFormat.class,
				Text.class, PairOfStringInt.class, Text.class, Text.class,
				Mapper.class, PageIdResolveReducer.class, reduceNo);
		job.getConfiguration().set("mapred.map.child.java.opts", "-Xmx9192M");
		job.setCombinerClass(Reducer.class);
		job.waitForCompletion(true);
		return output;
	}

	private static List<PairOfStrings> extractAnchoredLinks(
			WikipediaPage wikiPage) {
		String page = wikiPage.getRawXML();
		int start = 0;
		List<PairOfStrings> links = new ArrayList<PairOfStrings>();

		while (true) {
			start = page.indexOf("[[", start);

			if (start < 0)
				break;

			int end = page.indexOf("]]", start);

			if (end < 0)
				break;

			String text = page.substring(start + 2, end);
			String anchor = null, title = text;

			// skip empty links
			if (text.length() == 0) {
				start = end + 1;
				continue;
			}

			// skip special links
			if (text.indexOf(":") != -1) {
				start = end + 1;
				continue;
			}

			// get anchor text
			int a;
			if ((a = text.indexOf("|")) != -1) {
				title = text.substring(0, a);
				anchor = text.substring(a + 1);
			}

			if ((a = title.indexOf("#")) != -1) {
				title = title.substring(0, a);
			}

			// ignore article-internal links, e.g., [[#section|here]]
			if (title.length() == 0) {
				start = end + 1;
				continue;
			}
			title = title.trim();
			if (anchor == null)
				anchor = title;
			links.add(Pair.of(title, anchor));

			start = end + 1;
		}

		return links;
	}

	@SuppressWarnings("static-access")
	@Override
	public int run(String[] args) throws Exception {
		Options opts = new Options();

		Option langOpt = OptionBuilder.withArgName("lang").hasArg()
				.withDescription("language of the Wikipedia dump file")
				.create(LANG_OPTION);

		Option inputOpt = OptionBuilder.withArgName("input-path").hasArg()
				.withDescription("XML dump file path (required)")
				.create(INPUT_OPTION);

		Option outputOpt = OptionBuilder.withArgName("output-path").hasArg()
				.withDescription("output file path (required)")
				.create(OUTPUT_OPTION);

		Option reduceOpt = OptionBuilder.withArgName("reduce-no").hasArg()
				.withDescription("number of reducer nodes").create(REDUCE_NO);

		Option phaseOpt = OptionBuilder.withArgName("phase-no").hasArg()
				.withDescription("phase 1: Redirect resolve, Phase 2: Page ID resolve").create(PHASE);

		Option tmpOutput = OptionBuilder.withArgName("tmp-output").hasArg()
				.withDescription("Temporary output directory (required)")
				.create(TMP_OUTPUT_OPTION);
		
		Option phase1Out = OptionBuilder.withArgName("phase1-out").hasArg()
				.withDescription("Temporary output directory (required)")
				.create(PHASE1_OPTION);

		opts.addOption(langOpt);
		opts.addOption(inputOpt);
		opts.addOption(reduceOpt);
		opts.addOption(phaseOpt);
		opts.addOption(outputOpt);
		opts.addOption(tmpOutput);
		opts.addOption(phase1Out);

		CommandLine cl;
		CommandLineParser parser = new GnuParser();
		try {
			cl = parser.parse(opts, args);
		} catch (ParseException e) {
			System.err.println("Error parsing command line: " + e.getMessage());
			return -1;
		}
		if (!cl.hasOption(INPUT_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(getClass().getName(), opts);
			ToolRunner.printGenericCommandUsage(System.out);
			return -1;
		}
		if (!cl.hasOption(OUTPUT_OPTION)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(getClass().getName(), opts);
			ToolRunner.printGenericCommandUsage(System.out);
			return -1;
		}
		if (!cl.hasOption(OUTPUT_OPTION)) {
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
		if (cl.hasOption(LANG_OPTION)) {
			lang = cl.getOptionValue(LANG_OPTION);
		}

		String input = cl.getOptionValue(INPUT_OPTION);
		String output = cl.getOptionValue(OUTPUT_OPTION);
		String tmpOut = cl.getOptionValue(TMP_OUTPUT_OPTION);
		if (phase == 1) {
			phase1(input, reduceNo, lang, tmpOut);
		} else if (phase == 2) {
			String out;
			if (cl.hasOption(PHASE1_OPTION)) {
				out = cl.getOptionValue(PHASE1_OPTION);
			}
			else out = phase1(input, reduceNo, lang, tmpOut);
			String res = phase2(out, output, reduceNo);
			log.info("Write results to " + res);
		}
		return 0;
	}

	public static void main(String[] args) {
		try {
			ToolRunner.run(new BuildWikiAnchorText(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
