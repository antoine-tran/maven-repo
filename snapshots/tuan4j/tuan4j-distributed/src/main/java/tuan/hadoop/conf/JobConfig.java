package tuan.hadoop.conf;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.io.compress.Lz4Codec;
import org.apache.hadoop.io.compress.SnappyCodec;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuan.terrier.Files;
import tuan.terrier.HadoopDistributedFileSystem;

/**
 * A typical setting of one Hadoop job
 * @author tuan
 */
public class JobConfig extends Configured {

	private static final Logger log = LoggerFactory.getLogger(JobConfig.class);
	
	public static enum Version {
		HADOOP_1,
		HADOOP_2
	}

	private Version version = Version.HADOOP_2;

	private String mapperSize = "-Xmx1024m"; 

	public static final String INPUT_OPT = "in";
	public static final String OUTPUT_OPT = "out";
	public static final String REDUCE_NO = "reduce";
	public static final String JOB_NAME = "name";
	public static final String REMOVE_OUTPUT = "rmo";
	public static final String COMPRESS_OPT = "compress";
	protected CommandLine command;	
	
	private int reduceNo = 24;
	private String jobName;
	private String input;
	private String output;
	private String compressType = null;	
	private boolean removeOutputDirectory = false;
	
	@SuppressWarnings("static-access")
	public Options options() {
		Options opts = new Options();

		Option jnameOpt = OptionBuilder.withArgName("job-name").hasArg(true)
				.withDescription("Job name")
				.create(JOB_NAME);

		Option inputOpt = OptionBuilder.withArgName("input-path").hasArg(true)
				.withDescription("input file / directory path (required)")
				.create(INPUT_OPT);

		Option outputOpt = OptionBuilder.withArgName("output-path").hasArg(true)
				.withDescription("output file path (required)")
				.create(OUTPUT_OPT);

		Option reduceOpt = OptionBuilder.withArgName("reduce-no").hasArg(true)
				.withDescription("number of reducer nodes").create(REDUCE_NO);

		Option rmOpt = OptionBuilder.withArgName("remove-out").hasArg(false)
				.withDescription("remove the output then create again before writing files onto it")
				.create(REMOVE_OUTPUT);

		Option cOpt = OptionBuilder.withArgName("compress-option").hasArg(true)
				.withDescription("compression option").create(COMPRESS_OPT);

		opts.addOption(jnameOpt);
		opts.addOption(inputOpt);
		opts.addOption(reduceOpt);
		opts.addOption(outputOpt);
		opts.addOption(rmOpt);
		opts.addOption(cOpt);
		
		return opts;
	}
	
	public int parseOtions(String[] args) {
		Options opts = options();
		CommandLineParser parser = new GnuParser();
		try {
			command = parser.parse(opts, args);
		} catch (ParseException e) {
			System.err.println("Error parsing command line: " + e.getMessage());
			return -1;
		}

		if (!command.hasOption(INPUT_OPT) || !command.hasOption(OUTPUT_OPT)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(getClass().getName(), opts);
			ToolRunner.printGenericCommandUsage(System.out);
			return -1;
		}
		
		if (command.hasOption(REDUCE_NO)) {
			try {
				reduceNo = Integer.parseInt(command.getOptionValue(REDUCE_NO));
			} catch (NumberFormatException e) {
				System.err.println("Error parsing reducer number: "
						+ e.getMessage());
			}
		}
		
		if (command.hasOption(JOB_NAME)) {
			jobName = command.getOptionValue(JOB_NAME);
			jobName = jobName.replace('-',' ');
		} else {
			jobName = this.getClass().getCanonicalName();
		}
		
		if (command.hasOption(REMOVE_OUTPUT)) {
			markOutputForDeletion();
		}

		if (command.hasOption(COMPRESS_OPT)) {
			setCompress(command.getOptionValue(COMPRESS_OPT));
		}

		input = command.getOptionValue(INPUT_OPT);
		output = command.getOptionValue(OUTPUT_OPT);
		
		log.info("Job name: " + jobName);
		log.info(" - input: " + input);
		log.info(" - output file: " + output);
		log.info(" - compression: " + compressType);
		log.info(" - reducer no.: " + reduceNo);
		
		return 0;
	}
	
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
			
			// This is the nasty thing in MapReduce v2 and YARN: 
			// They always prefer their ancient jars first. 
			// Set this on to say you don't like it
			job.getConfiguration().set("mapreduce.job.user.classpath.first", "true");
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
	Job setup(Class<INFILE> inputFormatClass,
			Class<OUTFILE> outputFormatClass,
			Class<KEYIN> mapKeyOutClass,
			Class<VALUEIN> mapValOutClass,
			Class<KEYOUT> keyOutClass,
			Class<VALUEOUT> valOutClass,
			Class<REDUCER> reduceClass, String[] args) throws IOException {
		parseOtions(args);
		return setup(jobName, this.getClass(),
				input, output, inputFormatClass, outputFormatClass,
				mapKeyOutClass, mapValOutClass, keyOutClass, valOutClass,
				Mapper.class, reduceClass, reduceNo);
	}
	
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public <JOB, INFILE extends InputFormat, OUTFILE extends OutputFormat,
	KEYIN, VALUEIN, KEYOUT, VALUEOUT, 
	MAPPER extends Mapper, REDUCER extends Reducer, COMBINER extends Reducer>
	Job setup(
			Class<INFILE> inputFormatClass,
			Class<OUTFILE> outputFormatClass,
			Class<KEYIN> mapKeyOutClass,
			Class<VALUEIN> mapValOutClass,
			Class<KEYOUT> keyOutClass,
			Class<VALUEOUT> valOutClass,
			Class<MAPPER> mapClass,
			Class<REDUCER> reduceClass,
			Class<COMBINER> combinerClass,
			String[] args) throws IOException {
		parseOtions(args);
		return setup(jobName, this.getClass(),
				input, output, inputFormatClass, outputFormatClass,
				mapKeyOutClass, mapValOutClass, keyOutClass, valOutClass,
				mapClass, reduceClass, combinerClass, reduceNo);
	}
	
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public <JOB, INFILE extends InputFormat, OUTFILE extends OutputFormat,
	KEYIN, VALUEIN, KEYOUT, VALUEOUT, 
	MAPPER extends Mapper, REDUCER extends Reducer>
	Job setup(
			Class<INFILE> inputFormatClass,
			Class<OUTFILE> outputFormatClass,
			Class<KEYIN> mapKeyOutClass,
			Class<VALUEIN> mapValOutClass,
			Class<KEYOUT> keyOutClass,
			Class<VALUEOUT> valOutClass,
			Class<MAPPER> mapClass,
			Class<REDUCER> reduceClass,
			String[] args) throws IOException {
		parseOtions(args);
		return setup(jobName, this.getClass(),
				input, output, inputFormatClass, outputFormatClass,
				mapKeyOutClass, mapValOutClass, keyOutClass, valOutClass,
				mapClass, reduceClass, reduceNo);
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
	
	/** Register the HDFS filesystem to tuan.terrier.Files, so that any libraries use the class
	 * to open files can both read in local and HDFS file systems 
	 * @throws IOException */
	public void registerHDFS() throws IOException {
		if (!Files.hasFileSystemScheme(HadoopDistributedFileSystem.HDFS_SCHEME)) {
			Files.addFileSystemCapability(new HadoopDistributedFileSystem(getConf()));	
		}
	}
}