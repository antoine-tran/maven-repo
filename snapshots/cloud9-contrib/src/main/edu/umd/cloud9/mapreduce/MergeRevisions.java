/**
 * 
 */
package edu.umd.cloud9.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import tuan.hadoop.conf.JobConfig;

/**
 * @author tuan
 *
 */
public class MergeRevisions extends JobConfig implements Tool {
	
	private static final class MyMapper extends Mapper<LongWritable, Text, LongWritable, Text> {

		private static final LongWritable KEY = new LongWritable();
		private final JsonParser parser = new JsonParser();
		
		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			JsonObject obj =
					(JsonObject) parser.parse(value.toString());
			
			KEY.set(obj.get("pageid").getAsLong());
			
			context.write(KEY, value);
		}		
	}
	
	private static final class MyReducer extends Reducer<LongWritable, Text, NullWritable, Text> {

		@Override
		protected void reduce(LongWritable k, Iterable<Text> vals, Context context)
				throws IOException, InterruptedException {
			for (Text val : vals) {
				context.write(NullWritable.get(), val);
			}
		}		
	}
	
	@Override
	public int run(String[] args) throws Exception {
		Job job = setup(
				TextInputFormat.class, TextOutputFormat.class,
				NullWritable.class, Text.class,
				NullWritable.class, Text.class,
				MyMapper.class, MyReducer.class, args);
		
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

	public static void main(String[] args) {
		try {
			ToolRunner.run(new MergeRevisions(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
