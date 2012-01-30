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

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import javatools2.utils.Converter;

/**
 * This class extends Fabian's bulk inserter class by : <br>
 * 1 - providing the capability to process the ad-hoc data types (BLOB, CLOB, ROWID,SQLXML,...) <br>
 * 2 - support partial inserts
 * @author tuan
 *
 */
public class Inserter extends Database.Inserter {

	/** OVERRIDE: Tells after how many commands we will flush the batch */
	private int batchSize = 100;

	public Inserter(Database database, String table, int... columnTypes) throws SQLException {
		database.super(table, columnTypes);
	}

	// Because of the lack of polymorphism in Fabian's Inserter, I had to create a static method
	// for construction of new Inserter object with partial insert capacity
	public static Inserter newInserter(Database database, String table, Column... columns) throws SQLException {
		int[] columnTypes = new int[columns.length];
		StringBuilder sb = new StringBuilder(table);
		sb.append("(");
		for (int i = 0; i < columnTypes.length; i++) {
			columnTypes[i] = columns[i].getType();
			sb.append(columns[i].getName());
			if (i < columnTypes.length - 1) sb.append(",");
			else sb.append(")");
		}
		return new Inserter(database, sb.toString(), columnTypes);
	}

	@Override
	public void insert(Object...values) throws SQLException {
		newInsert(Arrays.asList(values));
	}

	public void newInsert(List<Object> values) throws SQLException {
		ByteArrayInputStream bis;
		byte[] buf;
		try {
			for (int i = 0; i < values.size(); i++) {

				// Insert into BLOB columb using BinaryStream
				if (columnTypes[i].getTypeCode() == Types.BLOB) {
					buf = Converter.toBytes(values.get(i));
					bis = new ByteArrayInputStream(buf);
					preparedStatement.setBlob(i + 1, bis, buf.length);
				}

				else preparedStatement.setObject(i + 1, values.get(i), columnTypes[i].getTypeCode());				
			}
			preparedStatement.addBatch();
		} 
		catch (Exception e) {
			throw new SQLException("Bulk-insert into " + tableName + " " + values + "\n" + e.getMessage());
		}
		if (batchCounter++ % this.batchSize == 0) flush();
	}
}
