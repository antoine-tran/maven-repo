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
 * LIFE - Extraction and Ranking of facts and relevant entities
 * of a person.
 * 
 * This source code is provided with AS IF - it does not guarantee the
 * or compatibilities with older or newer version of third-parties. In any
 * cases, if you have problems regarding using libraries delivered with
 * the project, feel free to write to the above email. Also, we would like
 * to get feedbacks from all of you
 *
 */
package javatools2.database.wrapper;

import java.sql.ResultSet;

import javatools.database.ResultIterator.ResultWrapper;

/**
 * @author tuan
 *
 */
public class ByteArrayWrapper implements ResultWrapper<byte[]> {

	public static final ByteArrayWrapper byteArrayWrapper = new ByteArrayWrapper();
	
	private ByteArrayWrapper(){}
	
	@Override
	public byte[] wrap(ResultSet r) throws Exception {
		return r.getBytes(1);
	}

}
