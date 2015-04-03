package edu.umd.cloud9.collection.wikipedia;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import edu.umd.cloud9.collection.wikipedia.language.EnglishWikipediaPage;
import tuan.hadoop.conf.JobConfig;

public class MergeWikipediaBinary extends JobConfig implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		Job job = setup(SequenceFileInputFormat.class, SequenceFileOutputFormat.class,
				IntWritable.class, EnglishWikipediaPage.class,
				IntWritable.class, EnglishWikipediaPage.class,
				Mapper.class, Reducer.class, args);
		
		job.getConfiguration().setClass("mapreduce.output.fileoutputformat.compress.codec", 
				BZip2Codec.class, CompressionCodec.class);
		job.getConfiguration().setClass("mapred.output.compression.codec", 
				BZip2Codec.class, CompressionCodec.class);

		job.getConfiguration().setClass("mapred.map.output.compression.codec", 
				BZip2Codec.class, CompressionCodec.class);
		job.getConfiguration().setClass("mapreduce.map.output.compress.codec", 
				BZip2Codec.class, CompressionCodec.class);
		
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
