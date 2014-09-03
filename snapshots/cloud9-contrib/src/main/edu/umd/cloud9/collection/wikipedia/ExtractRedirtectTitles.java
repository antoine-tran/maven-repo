package edu.umd.cloud9.collection.wikipedia;

import java.io.IOException;

import org.apache.commons.lang.WordUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

import tuan.hadoop.conf.JobConfig;

/** Extract redirect graphs where sources and destination are both titles */
public class ExtractRedirtectTitles extends JobConfig implements Tool {

	private static final class MyMapper extends Mapper<IntWritable, 
	WikipediaPage, Text, Text> {

		private Text from = new Text();
		private Text to = new Text();

		@Override
		protected void map(IntWritable pageId, WikipediaPage p, Context context)
				throws IOException, InterruptedException {
			if (p.isRedirect()) {
				from.set(p.getTitle());
				for (String t : p.extractLinkTargets()) {
					if (t.isEmpty()) {
						continue;
					}
					else {
						to.set(t);						
					}
				}
				context.write(from, to);
			}
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		// Extra command line options
		Job job = setup(SequenceFileInputFormat.class, MapFileOutputFormat.class, 
				IntWritable.class, WikipediaPage.class, Text.class, Text.class, 
				MyMapper.class, Reducer.class, args);
		job.waitForCompletion(true);		
		return 0;
	}

}
