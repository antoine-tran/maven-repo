package edu.umd.cloud9.collection.wikipedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import tuan.hadoop.conf.JobConfig;
import tuan.hadoop.io.IntPair;

public class ExtractContextFromExtractedWikipedia extends JobConfig implements Tool {

	private static final Pattern ANCHOR = Pattern.compile("<a href=\"(.*?)\".*?>(.*?)</a>");
	private static final Pattern WHITE_SPACE = Pattern.compile("\\s+");

	private static final class MyMapper extends Mapper<LongWritable, Text, 
	LongWritable, Text> {

		// Output: srcId TAB srcTitle TAB targets TAB anchor TAB preContext TAB posContext
		private Text VALUE = new Text();

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String raw = value.toString();
			
			int i = raw.indexOf("id=\"");
			int j = raw.indexOf("\"",i+4);
			String docid = raw.substring(i+4,j);

			i = raw.indexOf("title=\"",j+1);
			j = raw.indexOf("\">",i+7);
			String title = raw.substring(i+7,j);
			
			i = j;
			j = raw.indexOf("</doc></page>", raw.length() - 20);

			// Every presence of an anchor triggering one emit
			List<Anchor> anchorOffsets = new ArrayList<>();

			// the number of words read so far. It is also the
			// position of the next anchor if there are no words
			// in between it and the previous anchor
			int wordCnt = 0;

			// We tokenize between the links, so "end" also
			// points to the beginning offset of the next chunk
			// of text
			int start = i+1, end = start;

			// First scan: Get the offsets of the anchors, including starting
			// word offset and ending word offset of the anchors
			Matcher anchorFinder = ANCHOR.matcher(raw);
			Matcher spaceFinder = WHITE_SPACE.matcher(raw);
			while (anchorFinder.find()) {
				start = anchorFinder.start();
				String target = anchorFinder.group(1);
				String anchor = anchorFinder.group(2);
				int anchorCnt = anchor.split("\\s+").length;
				

				// no. of spaces of the text before the two consecutive anchors
				int tmpCnt = 0;
				if (end < start) {
					while (spaceFinder.find(end)) {
						if (spaceFinder.end() != start) {
							break;
						}
						if (spaceFinder.start() != end) {
							tmpCnt++;
						}
					}
				}
				Anchor ip = new Anchor(wordCnt + tmpCnt, anchorCnt, target);
				anchorOffsets.add(ip);
				end = anchorFinder.end();
				wordCnt = wordCnt + tmpCnt + anchorCnt;
			}

			// Second scan: Grab the context from the plain text
			int wordPos = -1;

			// We use a list of text buffers to cache the contexts
			List<ArrayList<String>> pre = new ArrayList<>();
			List<ArrayList<String>> pos = new ArrayList<>();
			List<ArrayList<String>> anchors = new ArrayList<>();

			for (@SuppressWarnings("unused")int k = 0; i < anchorOffsets.size(); k++) {
				pre.add(new ArrayList<String>());
				pos.add(new ArrayList<String>());
				anchors.add(new ArrayList<String>());
			}

			raw = raw.replaceAll("<a href=\"(.*?)\".*?>|</a>", "");
			spaceFinder = WHITE_SPACE.matcher(raw);
			int spaceBegin = 0, spaceEnd = 0;
			while (spaceFinder.find()) {
				if (spaceFinder.end() == j) {
					break;
				}
				spaceBegin = spaceFinder.start();
				if (spaceBegin != 0) {
					wordPos++;
					String word = raw.substring(spaceEnd, spaceBegin);

					for (int k = 0; k < anchorOffsets.size(); k++) {
						IntPair a = anchorOffsets.get(k);
						int dist = wordPos - a.getLeft() - a.getRight();
						if (dist >= 0 && dist < 50) {
							pos.get(k).add(word);
						}
						dist = a.getLeft() - wordPos;
						if (dist >= 0 && dist < 50) {
							pre.get(k).add(word);
						}
						if (a.getLeft() <= wordPos && a.getLeft() + a.getRight() > wordPos) {
							anchors.get(k).add(word);
						}
					}		
				}
			}
			
			// Finally emit the contexts
			for (int k = 0; k < anchorOffsets.size(); k++) {
				StringBuilder sb = new StringBuilder();
				sb.append(docid);
				sb.append("\t");
				sb.append(title);
				sb.append("\t");
				sb.append(anchorOffsets.get(k).text);
				sb.append("\t");
				for (String w : pre.get(k)) {
					sb.append(w);
				}
				sb.append("\t");
				for (String w : pos.get(k)) {
					sb.append(w);
				}
				VALUE.set(sb.toString());
				context.write(key, VALUE);
			}
		}
	}
	
	private static final class Anchor extends IntPair {
		String text;
		
		public Anchor(int l, int r, String t) {
			super(l,r);
			text = t;
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = setup(WikiExtractorInputFormat.class,TextOutputFormat.class,
				// PairOfStringInt.class, PairOfStrings.class,
				LongWritable.class, Text.class,
				//IntWritable.class,PairOfIntString.class,
				LongWritable.class, Text.class,
				MyMapper.class,
				// MyReducer1.class,
				// Mapper.class, 
				Reducer.class,
				args);

		job.getConfiguration().set("mapreduce.map.memory.mb", "6144");
		job.getConfiguration().set("mapreduce.reduce.memory.mb", "6144");
		job.getConfiguration().set("mapreduce.map.java.opts", "-Xmx6144m");
		job.getConfiguration().set("mapreduce.reduce.java.opts", "-Xmx6144m");
		job.getConfiguration().set("mapreduce.job.user.classpath.first", "true");
		job.waitForCompletion(true);

		return 0;
	}

	public static void main(String[] args) {
		try {
			ToolRunner.run(new ExtractContextFromExtractedWikipedia(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
