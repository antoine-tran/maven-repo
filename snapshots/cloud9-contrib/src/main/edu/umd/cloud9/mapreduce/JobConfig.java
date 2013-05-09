package edu.umd.cloud9.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * A typical setting of one Hadoop job
 * @author tuan
 */
public class JobConfig extends Configured {

	@SuppressWarnings("rawtypes")
	public <JOB, INFILE extends InputFormat, OUTFILE extends OutputFormat,
			KEYIN, VALUEIN, KEYOUT, VALUEOUT, 
			MAPPER extends Mapper, REDUCER extends Reducer> 
			Job setup(
				String jobName,	Class<JOB> jobClass, 
				String inpath, String outpath,
				Class<INFILE> inputFormatClass,
				Class<OUTFILE> outputFormatClass,
				Class<KEYIN> mapKeyOutClass,
				Class<VALUEIN> mapValOutClass,
				Class<KEYOUT> keyOutClass,
				Class<VALUEOUT> valOutClass,
				Class<MAPPER> mapClass,
				Class<REDUCER> reduceClass,
				int reduceNo) throws IOException {
		
		// Hadoop 2.0
		// Job job = Job.getInstance(getConf());
		// job.setJobName(jobName);

		// Hadoop 1.x
		Job job = new Job(getConf(), jobName);
		
		// Common configurations
		job.setJarByClass(jobClass);
		
		job.getConfiguration().setBoolean(
				"mapreduce.map.tasks.speculative.execution", false);
		job.getConfiguration().setBoolean(
				"mapreduce.reduce.tasks.speculative.execution", false);
		job.getConfiguration().set("mapreduce.child.java.opts", "-Xmx2048m");

		job.setNumReduceTasks(reduceNo);

		FileInputFormat.setInputPaths(job, new Path(inpath));
		FileOutputFormat.setOutputPath(job, new Path(outpath));
		
		job.setInputFormatClass(inputFormatClass);
		job.setOutputFormatClass(outputFormatClass);
		
		job.setMapOutputKeyClass(mapKeyOutClass);
		job.setMapOutputValueClass(mapValOutClass);
		
		job.setOutputKeyClass(keyOutClass);
		job.setOutputValueClass(valOutClass);
		
		job.setMapperClass(mapClass);
		job.setReducerClass(reduceClass);

		return job;
	}
}
