/**
 * ==================================
 * 
 * Copyright (c) 2010 Anh Tuan Tran
 *
 * URL: http://www.mpi-inf.mpg.de/~attran,
 *          http://www.l3s.de/~ttran
 *
 * Email: tranatuan24@gmail.com
 * ==================================
 * 
 * This source code is provided with AS IF - it does not guarantee the
 * or compatibilities with older or newer version of third-parties. In any
 * cases, if you have problems regarding using libraries delivered with
 * the project, feel free to write to the above email. Also, we would like
 * to get feedbacks from all of you
 */
package tuan.io;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import tuan.core.SerializableNull;
import tuan.core.ExceptionHandler;

/**
 * Set of small utility methods that handle frequent Java I/O operations
 * and help developers focus on algorithmic logic
 * 
 * @author tuan
 *
 */
public class FileUtility {
	
	/**
	 * Serialize an object into a file if the file does not exist yet
	 */
	public static boolean serialize(Object object, String fileName) throws IOException {
		
		File file = new File(fileName);
		
		if (file.exists()) return false;
		else {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
				oos.writeObject(object);
				oos.flush();
				oos.close();
				return true;
			}
			catch (IOException e) {
				file.delete();
				throw e;
			}
		}
	}
	
	/**
	 * Serialize an object into a file even if the file does exist.
	 */
	public static boolean forcedSerialize(Object object, String fileName) throws IOException {
		
		File file = new File(fileName);
		
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file,false)));
			oos.writeObject(object);
			oos.flush();
			oos.close();
			return true;
		}
		catch (IOException e) {
			file.delete();
			throw e;
		}
	}
	
	/**
	 * Read and return object from an input stream, or return SerializableNull
	 * @param fileName name of the object file
	 * @return null if an IOException happens, SerializableNull object if the file does not exist, 
	 *
	 * @throws An exception if otherwise
	 */
	public static Object read(String fileName) throws IOException, ClassNotFoundException {
		File file = new File(fileName);
		ObjectInputStream ois = null;
		
		if (!file.exists()) return SerializableNull.obj();
		else {
			try {
				ois = new ObjectInputStream(new FileInputStream(file));
				return ois.readObject();
			} 
			catch (IOException e) {
				e.printStackTrace();
				return null;
			}			
			finally {
				ois.close();
			}
		}
	}
	
	/**
	 * This class open a stream to a text file and read it line by line. It accepts
	 * a customized exception handler
	 */
	public static Iterable<String> readLines(String inputFileName, ExceptionHandler handler) {
		return new LineIterator(inputFileName, handler);	
	}
	
	static class LineIterator implements Iterator<String>, Iterable<String> {
		
		private BufferedReader reader;
		private ExceptionHandler handler;
		private String line;
		private boolean initialized = false;
		
		public LineIterator(String fileName, ExceptionHandler exceptionHandler) {
			this.handler = (exceptionHandler == null) ? new FileExceptionHandler() : exceptionHandler;
			
			try {
				Reader fileReader = new FileReader(fileName);
				reader = new BufferedReader(fileReader);
			} 
			catch (FileNotFoundException e) {				
				handler.handle(e);
			}
		}
		
		@Override
		public boolean hasNext() {
			if (!initialized) line = internalNext();
			
			if (line == null) {
				try {
					close(reader);
				} 
				catch (IOException e) {
					if (handler != null) handler.handle(e);
					else e.printStackTrace();
				}
			}
			return (line != null);
		}
		
		@Override
		public String next() {
			if (hasNext()) {
				String item = line;
				line = internalNext();
				return item;
			}
			else throw new NoSuchElementException();
		}
		
		private String internalNext() {
			try {
				String item = reader.readLine();
				if (!initialized) initialized = true;
				if (item == null) close(reader);
				return item;
			} 
			catch (IOException e) {
				if (handler != null) handler.handle(e);
				else e.printStackTrace();
				return null;
			}			
		}
		
		@Override
		public void remove() {
		}	
		
		private void close(Reader reader) throws IOException {
			reader.close();
		}
		
		@Override
		public Iterator<String> iterator() {
			return this;
		}
	}
	
	/**
	 * Merge many text file in a given directory that have names matching some patterns into one single file. 
	 */
	public static boolean mergeTextFiles(String dirName, FilenameFilter filter, String outputFile) throws IOException {
		
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"UTF8"));
		File dir = new File(dirName);
		
		if (!dir.exists()) return false;
		if (!dir.isDirectory()) return false;
		
		String[] files = dir.list(filter);
		BufferedReader bReader;
		InputStreamReader reader;
		FileInputStream fis;
		String tmp;
		
		for (String name : files) {
			
			fis = new FileInputStream(dirName + File.separator + name);
			
			reader = new InputStreamReader(fis);
			bReader = new BufferedReader(reader);
			
			while ((tmp = bReader.readLine()) != null) {
				out.write(tmp);
				out.write("\n");
			}
			out.flush();
			
			bReader.close();
			reader.close();
			fis.close();
		}
		out.close();
		return true;
	}
	
	/** efficiently copy content of a given file to another file. After that, close
	 * both file pointers */
	public static void copy( File fromFile, File toFile)
			throws IOException, FileNotFoundException, IllegalAccessException {
		
		if (fromFile == null || toFile == null) return;
		
		String fromFileName = fromFile.getName();
		String toFileName = toFile.getName();
		
		if (!fromFile.exists())
			throw new FileNotFoundException(fromFileName);
		if (!fromFile.isFile())
			throw new FileNotFoundException(fromFileName);
		if (!fromFile.canRead())
			throw new IllegalAccessException(fromFileName);
		
		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());
		
		if (toFile.exists()) {
			if (!toFile.canWrite())
				throw new IllegalAccessException( toFileName);

		} else {
			String parent = toFile.getParent();
			if (parent == null)
				parent = System.getProperty("user.dir");
			File dir = new File(parent);
			if (!dir.exists())
				throw new FileNotFoundException(parent);
			if (dir.isFile())
				throw new IllegalAccessException(parent);
			if (!dir.canWrite())
				throw new IllegalAccessException(parent);
		}
		
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;
			
			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead); // write
		} finally {
			from.close();
			to.close();
		}
	}
	
	protected static class FileExceptionHandler implements ExceptionHandler {
		
		public FileExceptionHandler() {}
		
		@Override
		public void handle(Throwable e) {
			e.printStackTrace();
		}

		@Override
		public void dispatch(Throwable e) throws Throwable {
			throw e;
		}		
	}
}
