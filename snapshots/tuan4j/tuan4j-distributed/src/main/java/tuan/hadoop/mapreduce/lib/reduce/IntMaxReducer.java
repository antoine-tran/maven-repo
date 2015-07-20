package tuan.hadoop.mapreduce.lib.reduce;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class IntMaxReducer<KEY> extends Reducer<KEY, IntWritable, KEY, IntWritable> {

	private final IntWritable minKey = new IntWritable();
	
	@Override
	protected void reduce(KEY k, Iterable<IntWritable> vs, Context context)
			throws IOException, InterruptedException {
		int min = Integer.MAX_VALUE;
		for (IntWritable v : vs) {
			int t = v.get();
			if (t < min) {
				min = t;
			}
		}
		minKey.set(min);
		context.write(k, minKey);
	}
}
