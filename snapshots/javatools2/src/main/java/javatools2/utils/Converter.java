/**
 * ==================================
 * Copyright (c) 2010 Max-Planck Institute for Informatics
 * Database and Information Systems Department
 * http://www.mpi-inf.mpg.de/departments/d5/index.html
 * 
 * Copyright (c) 2010 Anh Tuan Tran
 *
 * URL: http://www.mpi-inf.mpg.de/~attran
 *
 * Email: attran (at) mpi (dash) inf (dot) mpg (dot) de
 * ==================================
 * 
 * This source code is provided with AS IF - it does not guarantee the
 * or compatibilities with older or newer version of third-parties. In any
 * cases, if you have problems regarding using libraries delivered with
 * the project, feel free to write to the above email. Also, we would like
 * to get feedbacks from all of you
 *
 */
package javatools2.utils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * @author tuan
 *
 */
public class Converter {

	/**
	 * convert an object into a byte array
	 * @param obj
	 * @return
	 */
	public static byte[] toBytes(Object obj) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		oos.flush();
		oos.close();
		baos.close();
		return baos.toByteArray();
	}	
}
