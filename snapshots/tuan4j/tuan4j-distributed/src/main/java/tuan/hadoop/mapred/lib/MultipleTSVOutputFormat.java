package tuan.hadoop.mapred.lib;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat;

/**
 * A Lazier-than-Lazy MultipleOutputFormat that separates the outputs to
 * different directories in Hadoop streaming. The idea is that
 * each line streamed to stdout is prepended by a base output name, 
 * specifying a separate directory for which the named outputs are 
 * serialized. The output of  
 */
public class MultipleTSVOutputFormat extends MultipleTextOutputFormat<Text, Text> {

	@Override
	// the key is the file name, and the value is the concatenation of actual key
	// and value separated by the tab
	protected Text generateActualKey(Text key, Text value) {
		String v = value.toString();
		int t = v.indexOf('\t');
		key.set(v.substring(0, t));
		return key;
	}

	@Override
	// the key is the file name, and the value is the concatenation of actual key
	// and value separated by the tab
	protected Text generateActualValue(Text key, Text value) {
		String v = value.toString();
		int t = v.indexOf('\t');
		value.set(v.substring(t + 1));
		return value;
	}

	@Override
	// extract the first tab, output as a sub-directory inside the output path
	protected String generateFileNameForKeyValue(Text key, Text value, String name) {
		return new Path(key.toString(), name).toString();
	}
}
