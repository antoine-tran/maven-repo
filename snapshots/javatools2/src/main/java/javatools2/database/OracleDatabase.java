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

import java.io.File;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javatools.database.SQLType;


/**
 * This class replaces Fabian's OracleDatabase by implementing a newer version of Database class
 * @author tuan
 *
 */
public class OracleDatabase extends Database {

	/** ad-hoc property string of Oracle server */
	public static final String LOBs_ENABLED = "lobs";

	
	/** Prepares the query internally for a call (deletes trailing semicolon)*/
	@Override
	protected String prepareQuery(String sql) {
		if (sql.endsWith(";")) return (sql.substring(0, sql.length() - 1));
		else return (sql);
	}

	/** Constructs a non-functional OracleDatabase for use of getSQLType*/
	public OracleDatabase() {
		java2SQL.put(Boolean.class, bool);
		java2SQL.put(boolean.class, bool);
		java2SQL.put(String.class, varchar);
		java2SQL.put(Long.class, bigint);
		java2SQL.put(long.class, bigint);
		type2SQL.put(Types.VARCHAR, varchar);
		type2SQL.put(Types.NVARCHAR, varchar);
		type2SQL.put(Types.BOOLEAN, bool);
		type2SQL.put(Types.BIGINT, bigint);
	}

	/** Constructs a new OracleDatabase from a user, a password and a host*/
	public OracleDatabase(String user, String password, String host) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		this(user, password, host, null);
	}

	/** Constructs a new OracleDatabase from a user, a password and a host*/
	public OracleDatabase(String user, String password, String host, String port) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		this(user, password, host, null, null, null);
	}

	/** Constructs a new OracleDatabase from a user, a password and a host*/
	public OracleDatabase(String user, String password, String host, String port, String inst, Properties extraProps) throws SQLException {
		this(user, password, host, port, inst, extraProps, 8);
	}
	
	/** Constructs a new OracleDatabase from a user, a password and a host*/
	public OracleDatabase(String user, String password, String host, String port, String inst, Properties extraProps, int maxActive) throws SQLException {
		this();
		if (password == null) password = "";
		if (host == null) host = "localhost";
		if (port == null) port = "1521";
		if (inst == null) inst = "oracle";
		connectionString = "jdbc:oracle:thin:/@" + host + ":" + port + ":" + inst;
		Driver driver;
		try {
			driver = (Driver) Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		} catch (InstantiationException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
		DriverManager.registerDriver(driver);
		dataSource = setupDataSource(connectionString, user, password, maxActive);		
		fetchConnection(user, password, extraProps);
		description = "ORACLE database " + user + "/" + password + " at " + host + ":" + port + " instance " + inst;
	}

	/** Constructs a new OracleDatabase from a user and a password on the localhost*/
	public OracleDatabase(String user, String password) throws Exception {
		this(user, password, "localhost");
	}

	/** Makes an SQL query limited to n results */
	@Override
	public String limit(String sql, int n) {
		n++;
		Matcher m = Pattern.compile(" where", Pattern.CASE_INSENSITIVE).matcher(sql);
		if (!m.find()) {
			return (sql + " WHERE ROWNUM<" + n);
		}
		return (sql.substring(0, m.end()) + " ROWNUM<" + n + " AND " + sql.substring(m.end()));
	}

	@Override
	/** Returned DML statement with Oracle's ROWID parameter registered */
	public int returnedExecute(CharSequence sqlcs, int... param) throws SQLException {
		return returnedExecute(sqlcs, Types.NUMERIC, "ROWID", param);
	}
	
	@Override
	/**
	 * Rewrite the method createTable() to exploit / adapt special features in Oracle 11g
	 */
	public void createTable(String name, Object... attributes) throws SQLException {
		try {
			executeUpdate("BEGIN\nEXECUTE IMMEDIATE 'DROP TABLE " + name + "';\nEXCEPTION WHEN  OTHERS THEN NULL;\nEND;");
		} catch (SQLException e) {
		}
		StringBuilder b = new StringBuilder("CREATE TABLE ").append(name).append(" (");
		for (int i = 0; i < attributes.length; i += 2) {
			b.append(attributes[i]).append(' ');
			if (attributes[i + 1] instanceof Integer) {
				b.append(getSQLType((Integer) attributes[i + 1])).append(", ");
			} else {
				b.append(getSQLType((Class) attributes[i + 1])).append(", ");
			}
		}
		b.setLength(b.length() - 2);
		b.append(')');
		executeUpdate(b.toString());
	}

	@Override
	/** Loads a CSV file into a table. Rewrite to to exploit / adapt special features in Oracle 11g */
	public void loadCSV(String table, File input, boolean clearTable, char separator) throws IOException, SQLException {
		super.loadCSV(table, input, clearTable, separator);
	}
	
	@Override
	public int[] getColumnTypes(String table) throws SQLException {
		ResultSet r = query(limit("SELECT * FROM " + table, 1));
	      ResultSetMetaData meta = r.getMetaData();
	      int[] columnTypes = new int[meta.getColumnCount()];
	      for (int i = 0; i < columnTypes.length; i++) {
	        columnTypes[i] = meta.getColumnType(i + 1);
	      }
	      javatools.database.Database.close(r);
	      return columnTypes;
	}
	
	public static String enableBigStringProperties() {
		return "SetBigStringTryClob";
	}

	// -------------------------------------------------------------------------------
	// ------------------ Datatypes --------------------------------------------------
	// -------------------------------------------------------------------------------

	public static class Varchar extends SQLType.ANSIvarchar {

		public Varchar(int size) {
			super(size);
		}

		public Varchar() {
			super();
		}

		@Override
		public String toString() {
			return ("VARCHAR2(" + scale + ")");
		}

		@Override
		public String format(Object o) {
			String s = o.toString().replace("'", "''");
			if (s.length() > scale) s = s.substring(0, scale);
			return ("'" + s + "'");
		}
	}

	public static Varchar varchar = new Varchar();

	public static class Bool extends SQLType.ANSIboolean {

		public Bool() {
			super();
			typeCode = java.sql.Types.INTEGER;
		}

		@Override
		public String format(Object o) {
			if (super.format(o).equals("true")) return ("1");
			else return ("0");
		}

		@Override
		public String toString() {
			return ("NUMBER(1)");
		}
	}

	public static Bool bool = new Bool();

	public static class Bigint extends SQLType.ANSIBigint {

		@Override
		public String toString() {
			return ("NUMBER(22)");
		}
	}

	public static Bigint bigint = new Bigint();


}
