package edu.umd.cloud9.collection.wikipedia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.google.common.collect.Lists;

import edu.umd.cloud9.collection.wikipedia.WikipediaPage.ContextedLink;
import edu.umd.cloud9.io.pair.PairOfIntString;
import edu.umd.cloud9.io.pair.PairOfStringInt;
import edu.umd.cloud9.io.pair.PairOfStrings;

import tuan.hadoop.conf.JobConfig;

public class ExtractContextFromExtractedWikipedia extends JobConfig implements
Tool {

	private static final class MyMapper extends Mapper<LongWritable, Text, 
	PairOfStringInt, PairOfStrings> {

		private static final PairOfStringInt KEYPAIR = new PairOfStringInt();
		private static final PairOfStrings VALUEPAIR = new PairOfStrings();

		// Basic algorithm:
		// Emit: key = (link target article name, 0), value = (link target docid, "");
		// Emit: key = (link target article name, 1), value = (src docid, anchor text with context, offset and length)
		public void map(LongWritable key, Text p, Context context) throws IOException, InterruptedException {

			String raw = p.toString();
			int i = raw.indexOf("id=\"");
			int j = raw.indexOf("\"",i+5);
			String docid = raw.substring(i+4,j);
			VALUEPAIR.set(docid, "");

			i = raw.indexOf("title=\"",j+1);
			j = raw.indexOf("\">",i+5);
			String title = raw.substring(i+6,j);
			KEYPAIR.set(title, 0);
			context.write(KEYPAIR, VALUEPAIR);
			String fc = title.substring(0, 1);
			if (fc.matches("[A-Z]")) {
				title = title.replaceFirst(fc, fc.toLowerCase());

				KEYPAIR.set(title, 0);
				context.write(KEYPAIR, VALUEPAIR);
			}

			for (ContextedLink link : extractContextedLink(raw.substring(j+2,raw.length()-6))) {
				KEYPAIR.set(link.getTarget(), 1);
				VALUEPAIR.set(docid, link.getContext());
				context.write(KEYPAIR, VALUEPAIR);
			}
		}

		private List<ContextedLink> extractContextedLink(String page) {
			int start = 0;
			List<ContextedLink> links = Lists.newArrayList();

			while (true) {
				start = page.indexOf("<a href=", start);

				if (start < 0) {
					break;
				}

				int startText = page.indexOf('>', start);

				int end = page.indexOf("</a>", startText);

				if (end < 0) {
					break;
				}

				String text = page.substring(startText + 1, end);
				String anchor = page.substring(start + 9, startText - 1);

				int prefOffset = ((start > 500) ? start-500 : 0);
				int postOffset = (end + 500 > page.length() ? page.length() : end + 500);

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

				// if there is anchor text, get only article title
				int a;
				if ((a = text.indexOf("|")) != -1) {
					anchor = text.substring(a + 1, text.length());
					text = text.substring(0, a);
				}

				if ((a = text.indexOf("#")) != -1) {
					text = text.substring(0, a);
				}

				// ignore article-internal links, e.g., [[#section|here]]
				if (text.length() == 0) {
					start = end + 1;
					continue;
				}

				if (anchor == null) {
					anchor = text;
				}


				// Build the contexts around the anchor
				List<int[]> tokens = new ArrayList<>();

				ContextedLink cl = new ContextedLink(anchor, text);

				int tokenBegin = -1, tokenEnd = -1;
				for (int i = start-1; i > prefOffset; i--) {
					int c = page.codePointAt(i);
					int afterC = page.codePointAt(i);
					if (Character.isSpaceChar(c)) {

						if (tokenBegin > 0 && tokenEnd > 0) {
							tokens.add(new int[]{tokenBegin, tokenEnd});
							tokenBegin = tokenEnd = -1;
						}

						if (tokenEnd < 0) {
							tokenEnd = i;
						} else if (tokenBegin < 0 && Character.isSpaceChar(afterC)) {
							tokenEnd = i;
						} else if (tokenBegin < 0) {
							tokenBegin = i;
						} 
					}
				}

				// Reverse the order
				tokens = Lists.reverse(tokens);

				tokenBegin = tokenEnd = -1;
				for (int i = end+1; i < postOffset; i++) {
					int c = page.codePointAt(i);
					int beforeC = page.codePointAt(i-1);
					if (Character.isSpaceChar(c)) {

						if (tokenBegin > 0 && tokenEnd > 0) {
							tokens.add(new int[]{tokenBegin, tokenEnd});
							tokenBegin = tokenEnd = -1;
						}

						if (tokenBegin < 0) {
							tokenBegin = i;
						} else if (tokenEnd < 0 && Character.isSpaceChar(beforeC)) {
							tokenBegin = i;
						} else if (tokenEnd < 0) {
							tokenEnd = i;
						} 
					}
				}

				StringBuilder sb = new StringBuilder();
				for (int[] bound : tokens) {
					sb.append(page, bound[0]+1, bound[1]);
					sb.append(" ");
				}
				cl.setContext(sb.toString().replace('\t', ' '));
				cl.setStart(start);
				cl.setEnd(end);
				links.add(cl);

				start = end + 1;
			}

			return links;
		}

	}

	private static class MyPartitioner1 extends Partitioner<PairOfStringInt, PairOfStrings> {
		public int getPartition(PairOfStringInt key, PairOfStrings value, int numReduceTasks) {
			return (key.getLeftElement().hashCode() & Integer.MAX_VALUE) % numReduceTasks;
		}
	}
	
	private static class MyReducer1 extends Reducer<PairOfStringInt, 
			PairOfStrings, IntWritable, Text> {
		private static final IntWritable SRCID = new IntWritable();
		private static final Text TARGET_ANCHOR_PAIR 
		= new Text();

		private String targetTitle;
		private int targetDocid;

		public void reduce(PairOfStringInt key, Iterable<PairOfStrings> values,
				Context context) throws IOException, InterruptedException {

			if (key.getRightElement() == 0) {
				targetTitle = key.getLeftElement();
				for (PairOfStrings pair : values) {
					targetDocid = Integer.parseInt(pair.getLeftElement());
					break;
				}
			} else {
				if (!key.getLeftElement().equals(targetTitle)) {
					return;
				}

				for (PairOfStrings pair : values) {
					SRCID.set(Integer.parseInt(pair.getLeftElement()));
					TARGET_ANCHOR_PAIR.set(targetDocid + "\t" + pair.getRightElement());

					context.write(SRCID, TARGET_ANCHOR_PAIR);
				}
			}
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = setup(WikiExtractorInputFormat.class,TextOutputFormat.class,
				PairOfStringInt.class, PairOfStrings.class,
				IntWritable.class,PairOfIntString.class,
				MyMapper.class,MyReducer1.class,args);
		
		job.setPartitionerClass(MyPartitioner1.class);
		job.getConfiguration().set("mapreduce.map.memory.mb", "6144");
		job.getConfiguration().set("mapreduce.reduce.memory.mb", "6144");
		job.getConfiguration().set("mapreduce.map.java.opts", "-Xmx6144m");
		job.getConfiguration().set("mapreduce.reduce.java.opts", "-Xmx6144m");
		job.getConfiguration().set("mapreduce.job.user.classpath.first", "true");
		job.waitForCompletion(true);
		
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ToolRunner.run(new ExtractContextFromExtractedWikipedia(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
