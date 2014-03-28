package edu.umd.cloud9.collection.wikipedia.pig.load;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.pig.Expression;
import org.apache.pig.LoadFunc;
import org.apache.pig.LoadMetadata;
import org.apache.pig.ResourceSchema;
import org.apache.pig.ResourceStatistics;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.data.DataType;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.logicalLayer.schema.Schema;
import org.apache.pig.impl.logicalLayer.schema.Schema.FieldSchema;

import edu.umd.cloud9.collection.wikipedia.WikipediaPage;
import edu.umd.cloud9.collection.wikipedia.WikipediaPageInputFormat;

/**
 * This is a UDF loader that pipelines records in Wikipedia XML dump to Pig tuple, using
 * WikipediaPageInputFormat
 * @author tuan
 */
public class WikipediaXmlLoader extends LoadFunc implements LoadMetadata {

	private RecordReader<LongWritable, WikipediaPage> reader; 

	// do not create too many location paths, since setLocation() will be called by Pig many times
	private Path path = null;

	// a cached object that defines the output schema of a Wikipedia page
	private ResourceSchema schema;

	@SuppressWarnings("rawtypes")
	@Override
	public InputFormat getInputFormat() throws IOException {
		return new WikipediaPageInputFormat();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void prepareToRead(@SuppressWarnings("rawtypes") RecordReader reader, PigSplit split)
			throws IOException {
		this.reader = (RecordReader<LongWritable, WikipediaPage>)reader;		
	}

	@Override
	public Tuple getNext() throws IOException {
		boolean hasNext;
		try {
			hasNext = reader.nextKeyValue();
			if (hasNext) {
				WikipediaPage page = reader.getCurrentValue();
			}
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		return null;
	}

	@Override
	public void setLocation(String loc, Job job) throws IOException {

		// TODO: Test this and path.getName(), or find documentation specifying which one
		// should be used here
		if (path == null || !path.toString().equals(loc)) {
			path = new Path(loc);
			FileInputFormat.setInputPaths(job, path);
		}
	}

	@Override
	public String[] getPartitionKeys(String arg0, Job arg1) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceSchema getSchema(String loc, Job job) throws IOException {
		if (schema == null) {
			defineSchema();
		}
		return schema;
	}

	@Override
	public ResourceStatistics getStatistics(String arg0, Job arg1)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPartitionFilter(Expression arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	private void defineSchema() {
		Schema schema = new Schema();
		schema.add(new FieldSchema("title", DataType.CHARARRAY));
		schema.add(new FieldSchema("id", DataType.CHARARRAY));
		schema.add(new FieldSchema("text", DataType.CHARARRAY));
		schema.add(new FieldSchema("redirect", DataType.CHARARRAY));

		// wrapping each bag in a tuple

		Schema linkInfoSchema = new Schema();
		linkInfoSchema.add(new FieldSchema("target", DataType.CHARARRAY));
		linkInfoSchema.add(new FieldSchema("begin", DataType.INTEGER));
		linkInfoSchema.add(new FieldSchema("end", DataType.INTEGER));
		Schema linkInfoWrapper = new Schema(new FieldSchema ("t", linkInfoSchema));
		linkInfoWrapper.setTwoLevelAccessRequired(true);
		// schema.add(new FieldSchema("links", linkInfoWrapper, DataType.BAG));

		Schema headerInfoSchema = new Schema();
		headerInfoSchema.add(new FieldSchema("tagname", DataType.CHARARRAY));
		headerInfoSchema.add(new FieldSchema("begin", DataType.INTEGER));
		headerInfoSchema.add(new FieldSchema("end", DataType.INTEGER));
		Schema headerInfoWrapper = new Schema(new FieldSchema("t", headerInfoSchema));
		headerInfoWrapper.setTwoLevelAccessRequired(true);
		// schema.add(new FieldSchema("headers", headerInfoWrapper, DataType.BAG));

		Schema paragraphInfoSchema = new Schema();
		paragraphInfoSchema.add(new FieldSchema("tagname", DataType.CHARARRAY));
		paragraphInfoSchema.add(new FieldSchema("begin", DataType.INTEGER));
		paragraphInfoSchema.add(new FieldSchema("end", DataType.INTEGER));
		Schema paragraphInfoWrapper = new Schema(new FieldSchema("t", paragraphInfoSchema));
		paragraphInfoWrapper.setTwoLevelAccessRequired(true);
		// schema.add(new FieldSchema("paragraphs", paragraphInfoWrapper, DataType.BAG));
	}
}
