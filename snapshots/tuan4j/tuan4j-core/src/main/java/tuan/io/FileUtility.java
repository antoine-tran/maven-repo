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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;

import tuan.core.SerializableNull;
import tuan.core.ExceptionHandler;

/**
 * Set of small utility methods that handle frequent Java I/O operations
 * and help developers focus on algorithmic logic. It imports slurp methods
 * from Berkeley NLP util package
 * 
 * @author tuan
 * @author Dan Klein
 * @author Christopher Manning
 * @author Tim Grow (grow@stanford.edu)
 * @author Chris Cox
 * @author Johannes Hoffart
 * 
 * @version 2013/05/18
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
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			ObjectOutputStream oos = null;
			try {
				fos = new FileOutputStream(file);
				bos = new BufferedOutputStream(fos);
				oos = new ObjectOutputStream(bos);
				oos.writeObject(object);
				oos.flush();
				return true;
			}
			catch (IOException e) {
				file.delete();
				throw e;
			}
			finally {
				if (oos != null) oos.close();
				if (bos != null) bos.close();
				if (fos != null) fos.close();
			}
		}
	}

	/**
	 * Serialize an object into a file even if the file does exist.
	 */
	public static boolean forcedSerialize(Object object, String fileName) throws IOException {

		File file = new File(fileName);
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(file,false);
			bos = new BufferedOutputStream(fos);
			oos = new ObjectOutputStream(bos);
			oos.writeObject(object);
			oos.flush();
			oos.close();
			return true;
		}
		catch (IOException e) {
			file.delete();
			throw e;
		}
		finally {
			if (oos != null) oos.close();
			if (bos != null) bos.close();
			if (fos != null) fos.close();			
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
		FileInputStream fis = null;
		ObjectInputStream ois = null;

		if (!file.exists()) return SerializableNull.obj();
		else {
			try {
				fis = new FileInputStream(file);
				ois = new ObjectInputStream(fis);
				return ois.readObject();
			} 
			catch (IOException e) {
				e.printStackTrace();
				return null;
			}			
			finally {
				if (ois != null) ois.close();
				if (fis != null) fis.close();
			}
		}
	}

	/**
	 * This class open a stream to a text file and read it line by line. It accepts
	 * a customized exception handler
	 */
	public static Iterable<String> readLines(String inputFileName) {
		return new LineIterator(inputFileName, null);	
	}

	/**
	 * This class open a stream to a text file and read it line by line. It accepts
	 * a customized exception handler
	 */
	public static Iterable<String> readLines(String inputFileName, ExceptionHandler handler) {
		return new LineIterator(inputFileName, handler);	
	}

	/**
	 * This class open a stream to a text file and read it line by line. It accepts
	 * a customized exception handler
	 */
	public static Iterable<String> readLines(InputStream file, ExceptionHandler handler) {
		return new LineIterator(file, handler);	
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

		public LineIterator(InputStream file, ExceptionHandler exceptionHandler) {
			this.handler = (exceptionHandler == null) ? new FileExceptionHandler() : exceptionHandler;
			try {
				Reader fileReader = new BufferedReader(new InputStreamReader(file));
				reader = new BufferedReader(fileReader);	
			} catch (Exception e) {
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

		FileOutputStream fos = new FileOutputStream(outputFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
		BufferedWriter out = new BufferedWriter(osw);
		File dir = new File(dirName);

		if (!dir.exists()) return false;
		if (!dir.isDirectory()) return false;

		String[] files = dir.list(filter);
		BufferedReader bReader = null;
		InputStreamReader reader = null;
		FileInputStream fis = null;
		String tmp;

		try {
			for (String name : files) {
				try {
					fis = new FileInputStream(dirName + File.separator + name);

					reader = new InputStreamReader(fis);
					bReader = new BufferedReader(reader);

					while ((tmp = bReader.readLine()) != null) {
						out.write(tmp);
						out.write("\n");
					}
					out.flush();
				}
				finally {
					if (bReader != null) bReader.close();
					if (reader != null) reader.close();
					if (fis != null) fis.close();
				}
			}
		}
		finally {
			if (out != null) out.close();
			if (osw != null) osw.close();
			if (fos != null) fos.close();
		}
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

	private static final int SLURPBUFFSIZE = 16000;

	/**
	 * Returns all the text in the given File.
	 */
	public static String slurpFile(File file) throws IOException {
		Reader r = new FileReader(file);
		return slurpReader(r);
	}

	public static String slurpGBFileNoExceptions(String filename) {
		return slurpFileNoExceptions(filename, "GB18030");
	}

	/**
	 * Returns all the text in the given file with the given encoding.
	 */
	public static String slurpFile(String filename, String encoding)
			throws IOException {
		Reader r = new InputStreamReader(new FileInputStream(filename),
				encoding);
		return slurpReader(r);
	}

	/**
	 * Returns all the text in the given file with the given encoding. If the
	 * file cannot be read (non-existent, etc.), then and only then the method
	 * returns <code>null</code>.
	 */
	public static String slurpFileNoExceptions(String filename, String encoding) {
		try {
			return slurpFile(filename, encoding);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	public static String slurpGBFile(String filename) throws IOException {
		return slurpFile(filename, "GB18030");
	}

	/**
	 * Returns all the text from the given Reader.
	 * 
	 * @return The text in the file.
	 */
	public static String slurpReader(Reader reader) {
		BufferedReader r = new BufferedReader(reader);
		StringBuffer buff = new StringBuffer();
		try {
			char[] chars = new char[SLURPBUFFSIZE];
			while (true) {
				int amountRead = r.read(chars, 0, SLURPBUFFSIZE);
				if (amountRead < 0) {
					break;
				}
				buff.append(chars, 0, amountRead);
			}
			r.close();
		} catch (Exception e) {
			throw new RuntimeException();
		}
		return buff.toString();
	}

	/**
	 * Returns all the text in the given file
	 * 
	 * @return The text in the file.
	 */
	public static String slurpFile(String filename) throws IOException {
		return slurpReader(new FileReader(filename));
	}

	/**
	 * Returns all the text in the given File.
	 * 
	 * @return The text in the file. May be an empty string if the file is
	 *         empty. If the file cannot be read (non-existent, etc.), then and
	 *         only then the method returns <code>null</code>.
	 */
	public static String slurpFileNoExceptions(File file) {
		try {
			return slurpReader(new FileReader(file));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns all the text in the given File.
	 * 
	 * @return The text in the file. May be an empty string if the file is
	 *         empty. If the file cannot be read (non-existent, etc.), then and
	 *         only then the method returns <code>null</code>.
	 */
	public static String slurpFileNoExceptions(String filename) {
		try {
			return slurpFile(filename);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Instantiate Properties object from files in current directory or in class path
	 */
	public static Properties getPropertiesFromClasspath(String propFileName) throws IOException {

		// loading xmlProfileGen.properties from the classpath
		Properties props = new Properties();
		InputStream inputStream = FileUtility.class.getClassLoader()
				.getResourceAsStream(propFileName);

		if (inputStream == null) {
			throw new FileNotFoundException("property file '" + propFileName
					+ "' not found in the classpath");
		}

		props.load(inputStream);
		return props;
	}
	
	public static Properties getPropertiesFrom(String propFileName) throws IOException {

		// loading xmlProfileGen.properties from the classpath
		Properties props = new Properties();
		InputStream inputStream = new FileInputStream(propFileName);

		props.load(inputStream);
		return props;
	}
}
