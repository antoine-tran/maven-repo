package tuan.hadoop.conf;

import java.io.IOException;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.compress.Lz4Codec;
import org.apache.hadoop.io.compress.SnappyCodec;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.io.compress.CompressionCodec;
/**
 * A typical setting of one Hadoop job
 * @author tuan
 */
public class JobConfig extends Configured {

	public static enum Version {
		HADOOP_1,
		HADOOP_2
	}

	private Version version = Version.HADOOP_2;

	private boolean removeOutputDirectory = false;

	private String mapperSize = "-Xmx1024m";

	private String compressType = null;

	public void markOutputForDeletion() {
		removeOutputDirectory = true;
	}

	public void setMapperSize(String mapSize) {
		mapperSize = mapSize;
	}

	/**
	 * Compress type: gz, bz2, lz4, snappy, lzo
	 * @param type
	 */
	public void setCompress(String type) {
		compressType = type;
	}

	/**
	 * A simple job registration without any configuration
	 * @throws IOException 
	 */
	public <JOB> Job create(String jobName, Class<JOB> jobClass) throws IOException {
		// Hadoop 2.0
		Job job;
		if (version == Version.HADOOP_2) {
			job = Job.getInstance(getConf());
			job.setJobName(jobName);
		}

		// Hadoop 1.x
		else {
			job = new Job(getConf(), jobName);
		}
		
		job.setJarByClass(jobClass);
		
		return job;
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
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
			Class<REDUCER> reduceClass,
			int reduceNo) throws IOException {

		Job job = setup(jobName, jobClass,
				inpath, outpath, inputFormatClass, outputFormatClass,
				mapKeyOutClass, mapValOutClass, keyOutClass, valOutClass,
				Mapper.class, reduceClass, reduceNo);

		return job;
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
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

		Job job = create(jobName, jobClass);

		// Common configurations
		job.getConfiguration().setBoolean(
				"mapreduce.map.tasks.speculative.execution", false);
		job.getConfiguration().setBoolean(
				"mapreduce.reduce.tasks.speculative.execution", false);


		// Option: Java heap space
		job.getConfiguration().set("mapreduce.child.java.opts", mapperSize);
		job.getConfiguration().set("mapred.child.java.opts", mapperSize);

		setCompressOption(job);

		job.setNumReduceTasks(reduceNo);

		Path ip = new Path(inpath);
		Path op = new Path(outpath);

		if (removeOutputDirectory) {
			FileSystem fs = FileSystem.get(getConf());
			fs.delete(op, true);
		}

		FileInputFormat.setInputPaths(job, ip);
		FileOutputFormat.setOutputPath(job, op);

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

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public <JOB, INFILE extends InputFormat, OUTFILE extends OutputFormat,
	KEYIN, VALUEIN, KEYOUT, VALUEOUT, 
	MAPPER extends Mapper, REDUCER extends Reducer, COMBINER extends Reducer>
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
			Class<COMBINER> combinerClass,
			int reduceNo) throws IOException {

		Job job = setup(jobName, jobClass,
				inpath, outpath, inputFormatClass, outputFormatClass,
				mapKeyOutClass, mapValOutClass, keyOutClass, valOutClass,
				mapClass, reduceClass, reduceNo);

		job.setCombinerClass(combinerClass);

		setCompressOption(job);

		return job;
	}

	public void setCompressOption(Job job) {
		// Option: compress output
		if (compressType != null) {
			job.getConfiguration().setBoolean("mapreduce.output.fileoutputformat.compress", true);
			job.getConfiguration().setBoolean("mapred.output.compress", true);

			job.getConfiguration().set("mapreduce.output.fileoutputformat.compress.type", "BLOCK"); 
			job.getConfiguration().set("mapred.output.compression.type", "BLOCK"); 

			job.getConfiguration().setBoolean("mapred.compress.map.output", true); 
			job.getConfiguration().setBoolean("mapreduce.map.output.compress", true);

			if ("bz2".equals(compressType)) {
				getConf().setClass("mapreduce.output.fileoutputformat.compress.codec", 
						BZip2Codec.class, CompressionCodec.class);
				getConf().setClass("mapred.output.compression.codec", 
						BZip2Codec.class, CompressionCodec.class);

				getConf().setClass("mapred.map.output.compression.codec", 
						BZip2Codec.class, CompressionCodec.class);
				getConf().setClass("mapreduce.map.output.compress.codec", 
						BZip2Codec.class, CompressionCodec.class);
			}			
			else if ("gz".equals(compressType)) {
				getConf().setClass("mapreduce.output.fileoutputformat.compress.codec", 
						GzipCodec.class, CompressionCodec.class);
				getConf().setClass("mapred.output.compression.codec", 
						GzipCodec.class, CompressionCodec.class);

				getConf().setClass("mapred.map.output.compression.codec", 
						GzipCodec.class, CompressionCodec.class);
				getConf().setClass("mapreduce.map.output.compress.codec", 
						GzipCodec.class, CompressionCodec.class);
			}
			else if ("lz4".equals(compressType)) {
				getConf().setClass("mapreduce.output.fileoutputformat.compress.codec", 
						Lz4Codec.class, CompressionCodec.class);
				getConf().setClass("mapred.output.compression.codec", 
						Lz4Codec.class, CompressionCodec.class);

				getConf().setClass("mapred.map.output.compression.codec", 
						Lz4Codec.class, CompressionCodec.class);
				getConf().setClass("mapreduce.map.output.compress.codec", 
						Lz4Codec.class, CompressionCodec.class);
			}
			else if ("snappy".equals(compressType)) {
				getConf().setClass("mapreduce.output.fileoutputformat.compress.codec", 
						SnappyCodec.class, CompressionCodec.class);
				getConf().setClass("mapred.output.compression.codec", 
						SnappyCodec.class, CompressionCodec.class);

				getConf().setClass("mapred.map.output.compression.codec", 
						SnappyCodec.class, CompressionCodec.class);
				getConf().setClass("mapreduce.map.output.compress.codec", 
						SnappyCodec.class, CompressionCodec.class);
			}
			else if ("lzo".equals(compressType)) {
				getConf().set("mapreduce.output.fileoutputformat.compress.codec", 
						"com.hadoop.compression.lzo.LzoCodec");
				getConf().set("mapred.output.compression.codec", 
						"com.hadoop.compression.lzo.LzoCodec");

				getConf().set("mapred.map.output.compression.codec", 
						"com.hadoop.compression.lzo.LzoCodec");
				getConf().set("mapreduce.map.output.compress.codec", 
						"com.hadoop.compression.lzo.LzoCodec");
			}
			else throw new RuntimeException("Unknown compress codec: " + compressType);
		}
	}


	public void setVersion(Version v) {
		this.version = v;
	}
	
	public Version getVersion() {
		return version;
	}
}