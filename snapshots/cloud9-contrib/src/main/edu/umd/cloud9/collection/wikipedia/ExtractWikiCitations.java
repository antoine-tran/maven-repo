package edu.umd.cloud9.collection.wikipedia;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import tuan.hadoop.conf.JobConfig;

import edu.umd.cloud9.collection.wikipedia.WikipediaPage;

/**
 * This Hadoop job extracts the tweet citations from Wikipedia page 
 */
public class ExtractWikiCitations extends JobConfig implements Tool {

	private static Logger LOG = Logger.getLogger(ExtractWikiCitations.class);

	/** Debug counter: Number of documents emitted before the job stops.
	 *  -1 for no limits */
	private static enum PageCounter {
		WIKIPAGE_COUNTER, WIKIPAGE_MARKUP_COUNTER;
	}	
	private static final long MAX_WIKI_PAGES_CNT = -1l;

	public static final String INPUT_OPTION = "input";
	public static final String OUTPUT_OPTION = "output";
	public static final String LANGUAGE_OPTION = "wiki_language";
	public static final String CITATION_OPTION = "cite";


	/** Extract citations, get all the way back to the beginning of the paragraph */
	private static final Pattern TWITTER_CITATION =
			Pattern.compile("(?<=\\n)(.*?(\\{\\{[c|C]ite.*\\|)([uU][rR][lL](\\s)*=(\\s)*)?([^\\s]*twitter\\.com\\/[^\\s]*\\/status\\/[^\\s]*)(.*?)\\}\\})");
	
	private static final Pattern GENERAL_CITATION =
			Pattern.compile("(?<=\\n)(.*?(\\{\\{[c|C]ite.*\\|)([uU][rR][lL](\\s)*=(\\s)*)?([^\\s]*http[s]?:\\/\\/[^\\s]*)(.*?)\\}\\})");

	private static final class ExtractGeneralCitationMapper extends Mapper<LongWritable, WikipediaPage, Text, Text> {

		private final Text key = new Text();
		private final Text val = new Text();
		
		@Override
		protected void map(LongWritable k, WikipediaPage p,
				Context context) throws IOException, InterruptedException {
			
			// dont process if there are enough pages emitted
			Counter pageCnt = context.getCounter(PageCounter.WIKIPAGE_COUNTER);
			if (MAX_WIKI_PAGES_CNT > 0 && pageCnt.getValue() > MAX_WIKI_PAGES_CNT) return;			
			
			String docId = p.getDocid();
			if (docId == null || docId.isEmpty()) return;
			key.set(docId);

			String raw = p.getWikiMarkup();
			if (raw == null || raw.isEmpty()) return;

			String title = p.getTitle();
			if (title == null || title.isEmpty()) return;

			boolean found = false;
			Matcher m = GENERAL_CITATION.matcher(raw);
			while (m.find()) {
				if (!found) found = true;

				String citation = m.group();
				if (citation == null || citation.isEmpty()) continue;

				String url = m.group(6);
				if (url == null || url.isEmpty()) continue;
				
				key.set(docId);
				val.set(url + "\t" + title + "\t" + citation.trim().replace('\n', ' '));
				context.write(key, val);
			}

			// count the number of Wikipedia pages that pass
			if (found) context.getCounter(PageCounter.WIKIPAGE_COUNTER).increment(1);
		}
	}

	private static final class ExtractTweetCitationMapper extends Mapper<LongWritable, WikipediaPage, Text, Text> {

		private final Text key = new Text();
		private final Text val = new Text();

		@Override
		protected void map(LongWritable id, WikipediaPage p, Context c) throws IOException, InterruptedException {

			// dont process if there are enough pages emitted
			Counter pageCnt = c.getCounter(PageCounter.WIKIPAGE_COUNTER);
			if (MAX_WIKI_PAGES_CNT > 0 && pageCnt.getValue() > MAX_WIKI_PAGES_CNT) return;

			String docId = p.getDocid();
			if (docId == null || docId.isEmpty()) return;
			key.set(docId);

			String raw = p.getWikiMarkup();
			if (raw == null || raw.isEmpty()) return;

			String title = p.getTitle();
			if (title == null || title.isEmpty()) return;

			boolean found = false;
			Matcher m = TWITTER_CITATION.matcher(raw);
			while (m.find()) {
				if (!found) found = true;

				String citation = m.group();
				if (citation == null || citation.isEmpty()) continue;

				String url = m.group(6);
				if (url == null || url.isEmpty()) continue;

				key.set(docId);
				val.set(url + "\t" + title + "\t" + citation.trim().replace('\n', ' '));
				c.write(key, val);				
			}

			// count the number of Wikipedia pages that pass
			if (found) c.getCounter(PageCounter.WIKIPAGE_COUNTER).increment(1);
		}		
	}

	@SuppressWarnings("static-access")
	@Override
	public int run(String[] args) throws Exception {

		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("path")
				.hasArg().withDescription("XML dump file").create(INPUT_OPTION));
		options.addOption(OptionBuilder.withArgName("path")
				.hasArg().withDescription("output file").create(OUTPUT_OPTION));		
		options.addOption(OptionBuilder.withArgName("en|sv|de|cs|es|zh|ar|tr").hasArg()
				.withDescription("two-letter language code").create(LANGUAGE_OPTION));		
		options.addOption(OptionBuilder.withArgName("tw|ge").hasArg()
				.withDescription("extract any citations or only tweets").create(CITATION_OPTION));	
		
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

		String language = "en";
		if (cmdline.hasOption(LANGUAGE_OPTION)) {
			language = cmdline.getOptionValue(LANGUAGE_OPTION);
			if(language.length()!=2){
				System.err.println("Error: \"" + language + "\" unknown language!");
				return -1;
			}
		}
		
		String cite = "tw";
		if (cmdline.hasOption(CITATION_OPTION)) {
			cite = cmdline.getOptionValue(CITATION_OPTION);
		}

		String inputPath = cmdline.getOptionValue(INPUT_OPTION);
		String outputFile = cmdline.getOptionValue(OUTPUT_OPTION);

		LOG.info("Tool name: " + this.getClass().getName());
		LOG.info(" - input: " + inputPath);
		LOG.info(" - output file: " + outputFile);
		LOG.info(" - language: " + language);
		LOG.info(" - cite type: " + cite);

		Job job = setup("Build Wikipedia Anchor text graph. Phase 1", 
				ExtractWikiCitations.class, inputPath, outputFile, 
				WikipediaPageInputFormat.class, TextOutputFormat.class,
				Text.class, Text.class, Text.class, Text.class,
				("tw".equals(cite)) ? ExtractTweetCitationMapper.class : ExtractGeneralCitationMapper.class, 
				Reducer.class, 0);

		job.getConfiguration().set("mapred.map.child.java.opts", "-Xmx4096M");
		job.waitForCompletion(true);
		return 0;
	}

	public static void main(String[] args) {
		String s1 = "\nThe move has been criticized by [[Mohamed ElBaradei]] " +
				"who said Morsi had \"usurped all state powers and appointed himself Egypt's new [[pharaoh]]\"." +
				"<ref>{{cite web|Url =https://twitter.com/ElBaradei/status/271656968341581824|title=Twitter / " +
				"ELBaradei |date=22 November 2012 |accessdate=23 November 2012}}</ref>fsdf <ref>{{cite web|" +
				"url=https://twitter.com/ElBaradei/status/271656968341581824 |title = Twitter / " +
				"ELBaradei |date=22 November 2012 |accessdate=23 November 2012}}</ref>\n<ref>" +
				"{{Cite web|Url=http://www.twitter.com/world/egypts-president-morsi-takes-" +
				"sweeping-new-powers/2012/11/status/22/8d87d716-34cb-11e2-92f0-496af208bf23_story.html |" +
				"title=Egypt’s President Morsi takes sweeping new powers |publisher\"Washington Post |date=22 " +
				"November 2012 |accessdate=23 November 2012}}</ref>";
		String s2 = "{{cite web|url=https://twitter.com/ElBaradei/status/271656968341581824 |title = Twitter " +
				"/ ELBaradei |date=22 November 2012 |accessdate=23 November 2012}}";
		System.out.println(GENERAL_CITATION.matcher(s2).matches());
		Matcher m = GENERAL_CITATION.matcher(s1);
		while (m.find()) {
			System.out.println(m.group());
			System.out.println("======================");
			System.out.println(m.group(6));
		}
		/*try {
			ToolRunner.run(new ExtractWikiCitations(), args);
		} catch (Exception e) { 
			e.printStackTrace();
		}*/
	}
}
