package tuan.hadoop.io;

import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class TestIO extends Configured implements Tool {

	public void testNestedDirList(final String[] args) throws IOException {

		FileSystem fs = FileSystem.get(getConf());
		
		final FileStatus[] files = fs.listStatus(new Path(args[0]), 
				new org.apache.hadoop.fs.PathFilter()
				{ 
					public boolean accept(Path path)
					{					
						final String name = path.getName();
						//1. is this a run file
						if (!(  name.startsWith(args[1])  && name.endsWith(".runs")))
							return false;
						return true;
					}
				}
			);

			if (files == null || files.length == 0)
			{
				throw new IOException("No run status files found in "+args[0]);
			}
	}
	
	@Override
	public int run(String[] args) throws Exception {
		
		// Test nested directory listing
		testNestedDirList(args);
		
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ToolRunner.run(new TestIO(), args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}