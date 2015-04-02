package edu.umd.cloud9.collection.wikipedia;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import tuan.hadoop.conf.JobConfig;

public class MergeWikipediaBinary extends JobConfig implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		Job job = setup("merging wikipedia file", MergeWikipediaBinary.class,
				input, output,
				SequenceFileInputFormat.class, SequenceFileOutputFormat.class,
				IntWritable.class, WikipediaPage.class,
				IntWritable.class, WikipediaPage.class,
				Mapper.class, Reducer.class, 70);
		
		job.waitForCompletion(true);
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ToolRunner.run(new MergeWikipediaBinary(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
