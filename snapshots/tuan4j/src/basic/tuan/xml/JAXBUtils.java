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
package tuan.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * This class wraps the JAXB marshalling and unmarshalling into
 * utility methods with generic flexibility
 * @author tuan
 *
 */
public class JAXBUtils {

	private static JAXBContext jc = null; 
	private static Marshaller m = null;
	private static Unmarshaller um = null;
	
	private static final <T> void initMarshaller(Class<T> c) 
			throws JAXBException {
		jc = JAXBContext.newInstance(new Class[]{c});
		m = jc.createMarshaller();
	}
	
	private static final <T> void initUnmarshaller(Class<T> c) 
			throws JAXBException {
		jc = JAXBContext.newInstance(new Class[]{c});
		um = jc.createUnmarshaller();
	}	
	
	public static <T> void marshallToFile(String file, Object jaxbElement, 
			Class<T> c) throws JAXBException, IOException {
		initMarshaller(c);
		FileOutputStream fos = new FileOutputStream(file);
		m.marshal(jaxbElement, fos);
		fos.close();
	}
	
	public static <T> void marshallToFile(File file, Object jaxbElement, 
			Class<T> c) throws JAXBException, IOException {
		initMarshaller(c);
		FileOutputStream fos = new FileOutputStream(file);
		m.marshal(jaxbElement, fos);
		fos.close();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T unmarshall(String file, Class<T> c) throws IOException, 
			JAXBException {
		FileInputStream fis = new FileInputStream(file);
		initUnmarshaller(c);
		return (T) um.unmarshal(fis);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T unmarshall(InputStream input, Class<T> c) 
			throws IOException, JAXBException {
		initUnmarshaller(c);
		return (T) um.unmarshal(input);
	}
}
