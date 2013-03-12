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
package edu.stanford.nlp.util;

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

import edu.stanford.nlp.io.SerializableNull;


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
 * @version 2003/02/03
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
}
