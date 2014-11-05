/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.mapreduce.lib.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.LineReader;

/**
 * Re- port the NLineInputFormat into a new Mapreduce API
 */

public class NLineInputFormat extends FileInputFormat<LongWritable, Text> { 

	private int N = 1;

	@Override
	public RecordReader<LongWritable, Text> createRecordReader(InputSplit input,
			TaskAttemptContext tac) throws IOException, InterruptedException {
		return new LineRecordReader();
	}
	
	@Override
	public List<InputSplit> getSplits(JobContext context) throws IOException {
		Configuration conf = context.getConfiguration();
		ArrayList<InputSplit> splits = new ArrayList<InputSplit>();
		for (FileStatus status : listStatus(context)) {
			Path fileName = status.getPath();
			if (status.isDirectory()) {
				throw new IOException("Not a file: " + fileName);
			}
			FileSystem  fs = fileName.getFileSystem(conf);
			LineReader lr = null;
			try {
				FSDataInputStream in  = fs.open(fileName);
				lr = new LineReader(in, conf);
				Text line = new Text();
				int numLines = 0;
				long begin = 0;
				long length = 0;
				int num = -1;
				while ((num = lr.readLine(line)) > 0) {
					numLines++;
					length += num;
					if (numLines == N) {
						splits.add(createFileSplit(fileName, begin, length));
						begin += length;
						length = 0;
						numLines = 0;
					}
				}
				if (numLines != 0) {
					splits.add(createFileSplit(fileName, begin, length));
				}

			} finally {
				if (lr != null) {
					lr.close();
				}
			}
		}
		return splits;
	}

	/**
	 * NLineInputFormat uses LineRecordReader, which always reads
	 * (and consumes) at least one character out of its upper split
	 * boundary. So to make sure that each mapper gets N lines, we
	 * move back the upper split limits of each split 
	 * by one character here.
	 * @param fileName  Path of file
	 * @param begin  the position of the first byte in the file to process
	 * @param length  number of bytes in InputSplit
	 * @return  FileSplit
	 */
	protected static FileSplit createFileSplit(Path fileName, long begin, long length) {
		return (begin == 0) 
				? new FileSplit(fileName, begin, length - 1, new String[] {})
		: new FileSplit(fileName, begin - 1, length, new String[] {});
	}
}