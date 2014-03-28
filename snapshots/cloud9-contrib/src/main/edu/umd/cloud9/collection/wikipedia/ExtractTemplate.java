/**
 * 
 */
package edu.umd.cloud9.collection.wikipedia;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.util.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuan.hadoop.conf.JobConfig;

/**
 * @author tuan
 *
 */
public class ExtractTemplate extends JobConfig implements Tool {

	private static final Logger LOG = LoggerFactory.getLogger(ExtractTemplate.class);
	
	private static final String LANG_OPT = "lang";
	private static final String INPUT_OPT = "in";
	private static final String OUTPUT_OPT = "out";
	private static final String REDUCE_NO = "reduce";	
	private static final String TITLE_ID_MAP_OPT = "idmap";
	private static final String TMP_DIR_OPT = "tmpdir";	
	private static String TMP_HDFS_DIR = "/user/tuan.tran/tmp/";
	
	private static final class ExtractTemplateMapper extends Mapper<LongWritable, WikipediaPage, 
			LongWritable, Text> {
		
		@Override
		protected void setup(Context context) throws IOException,
				InterruptedException {
			context.getConfiguration().get("");
			super.setup(context);
		}

		@Override
		protected void map(LongWritable key, WikipediaPage p,
				Context context) throws IOException, InterruptedException {
			
			LOG.debug("Processing page: " + p.getDocid());
			
			super.map(key, p, context);
		}
		
		
	}
	
	@Override
	public int run(String[] arg0) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

}
