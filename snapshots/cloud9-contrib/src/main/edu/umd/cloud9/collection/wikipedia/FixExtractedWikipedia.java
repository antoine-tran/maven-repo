package edu.umd.cloud9.collection.wikipedia;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import tuan.hadoop.conf.JobConfig;

public class FixExtractedWikipedia extends JobConfig implements Tool {

	private static final Logger LOG = Logger.getLogger(ExtractContextFromExtractedWikipedia.class);

	private static final Pattern ANCHOR = Pattern.compile("<a href=\"(.*?)\".*?>(.*?)</a>");
	private static final Pattern WHITE_SPACE = Pattern.compile("\\s+");

	private static final class MyMapper extends Mapper<LongWritable, Text, 
	NullWritable, Text> {

		private Text VALUE = new Text();
		
		@Override
		protected void map(LongWritable key, Text value, final Context context)
				throws IOException, InterruptedException {

			String raw = value.toString();

			// because we heavily rely on white spaces to detect context, we
			// must make sure there are spaces between the anchors
			raw = raw.replace("<a href", " <a href");
			raw = raw.replace("</a>", "</a> ");

			int i = raw.indexOf("id=\"");
			int j = raw.indexOf("\"",i+4);
			String docid = raw.substring(i+4,j);

			i = raw.indexOf("title=\"",j+1);
			j = raw.indexOf("\">",i+7);
			String title = raw.substring(i+7,j);
			
			if (title.contains("Ascorbate ferrireductase (transmembrane)")) {
				raw = raw.replace("\n<page>", "");
			}
			VALUE.set(raw);
			context.write(NullWritable.get(), VALUE);
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		Job job = setup(WikiExtractorInputFormat.class,TextOutputFormat.class,
				// PairOfStringInt.class, PairOfStrings.class,
				NullWritable.class, Text.class,
				//IntWritable.class,PairOfIntString.class,
				NullWritable.class, Text.class,
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
			ToolRunner.run(new FixExtractedWikipedia(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}