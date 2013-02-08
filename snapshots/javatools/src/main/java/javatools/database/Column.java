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
package javatools.database;

import javatools.database.SQLType;

/**
 * A Java representation of one schema column
 * @author tuan
 *
 */

public class Column {
	private String name;
	private int type;

	public Column(String name, int type) {		
		this.name = name;
		this.type = type;
	}
	
	public Column(String name, SQLType sqlType) {
		this.name = name;
		this.type = sqlType.getTypeCode();
	}
	
	public String getName() {
		return name;
	}
	public int getType() {
		return type;
	}
}
