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
package javatools2.database;

/**
 * @author tuan
 *
 */
public class Value {
	private Object value;
	private int type;

	public Value(Object value, int type) {
		super();
		this.value = value;
		this.type = type;
	}
	public Object getValue() {
		return value;
	}
	public int getType() {
		return type;
	}
}
