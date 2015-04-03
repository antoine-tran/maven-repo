package edu.umd.cloud9.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import tuan.hadoop.conf.JobConfig;

public class MergeText extends JobConfig implements Tool {

	private static final class MyMapper extends Mapper<LongWritable, Text, NullWritable, Text> {

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			context.write(NullWritable.get(), value);
		}		
	}
	
	@Override
	public int run(String[] args) throws Exception {
		Job job = setup(
				TextInputFormat.class, TextOutputFormat.class,
				NullWritable.class, Text.class,
				NullWritable.class, Text.class,
				MyMapper.class, Reducer.class, args);
		
		job.waitForCompletion(true);
		return 0;
		
	}

	public static void main(String[] args) {
		try {
			ToolRunner.run(new MergeText(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
