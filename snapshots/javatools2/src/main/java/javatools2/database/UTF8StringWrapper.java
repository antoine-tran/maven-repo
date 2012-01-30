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
package javatools2.database;

import java.nio.charset.Charset;
import java.sql.ResultSet;

import javatools.database.ResultIterator.ResultWrapper;

/**
 * @author tuan
 *
 */
public class UTF8StringWrapper implements ResultWrapper<String> {

	private final String encoding = "UTF-8";
	
	private UTF8StringWrapper() {
	}
	
	public static UTF8StringWrapper instance() {
		return new UTF8StringWrapper();
	}
	
	@Override
	public String wrap(ResultSet r) throws Exception {
		return new String(r.getBytes(1), Charset.forName(encoding));
	}

}
