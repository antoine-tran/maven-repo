package edu.umd.cloud9.collection.wikipedia;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.Tool;
import org.arabidopsis.ahocorasick.AhoCorasick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.tokensregex.MultiWordStringMatcher;
import edu.stanford.nlp.ling.tokensregex.MultiWordStringMatcher.MatchType;

import tuan.hadoop.conf.JobConfig;

/** 
 * This tool calculates the "keyphraseness" of a set of words 
 * based on Wikipedia, using the method reported in the paper
 * "Wikify! Linking Documents to Encyclopedic Knowledge", 
 * Mihalcea et al. CIKM 2007 
 * 
 * @author tuan
 */
public class BuildWikiKeyphrase extends JobConfig implements Tool {
	
	private static final Logger log = 
			LoggerFactory.getLogger(BuildWikiKeyphrase.class);

	/** Use Aho-Corasick algorithm to search for the occurrences of 
	 * any phrase in the wikipedia text */
	private static final class AhoCorasickMapper 
			extends Mapper<LongWritable, WikipediaPage, Text, IntWritable> {
		
		private static final AhoCorasick tree = new AhoCorasick();
		
		private static final MultiWordStringMatcher mwsm = new MultiWordStringMatcher(MatchType.LNRM);

		private static final Map<String, String> lnrmMap = new HashMap<String, String>();
		
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			String path = context.getConfiguration().get("wiki.id.cache");
			FileSystem fs = FileSystem.get(context.getConfiguration());
			Path p = new Path(path);
			Configuration c = context.getConfiguration();
			loadKeyphrase(p, c, fs);
		}
		
		private void loadKeyphrase(Path p, Configuration c, FileSystem fs) throws IOException {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(fs.open(p)));
				String line;
				while ((line = reader.readLine()) != null) {
					
				}
			} finally {
				IOUtils.closeStream(reader);
			}
		}
		
		@Override
		protected void map(LongWritable key, WikipediaPage value,
				Context context) throws IOException, InterruptedException {
			// TODO Auto-generated method stub
			super.map(key, value, context);
		}

	}
	
	@Override
	public int run(String[] arg0) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	
} 
