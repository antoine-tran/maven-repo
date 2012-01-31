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

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class replaces Fabian's MySQLDatabase by implementing a newer version of Database class
 * @author tuan
 *
 */
public class MySQLDatabase extends Database {
	
	/** Constructs a new MySQLDatabase from a user and a password, all other arguments may be null*/
	public MySQLDatabase(String user, String password, String database, String host, String port, String charset, String collate) 
	throws SQLException {
		this(user, password, database, host, port, charset, collate, 8);
	}
	
	/** Constructs a new MySQLDatabase from a user and a password, all other arguments may be null*/
	public MySQLDatabase(String user, String password, String database, String host, String port, String charset, String collate, int maxActive) 
	throws SQLException {
		Driver driver;
		try {
			driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
		DriverManager.registerDriver(driver);
		if (host == null) host = "localhost";
		if (database == null) database = "enwiki";
		if (port == null) port = "";
		else port = ":" + port;
		if (charset == null) charset = "";
		else charset = "&useUnicode=true&characterEncoding=" + charset + "&characterSetResults=" + charset;
		if (collate == null) collate = "";
		else collate = "&connectionCollation=" + collate;
		dataSource = setupDataSource(connectionString, user, password, maxActive);		
		connectionString = String.format("jdbc:mysql://%s%s/%s?user=%s&password=%s%s%s", host, port, database, user, password, charset, collate);
		fetchConnection(user, password, null);
	}
	
	/** Constructs a new EMBEDDED MySQLDatabase from a user and a password 
	 * all other arguments may be null*/
	public MySQLDatabase(String user, String password, String database, String host, String port, String charset, String collate, String embedded) 
	throws SQLException {
		Driver driver;
		try {
			driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
		DriverManager.registerDriver(driver);
		if (host == null) host = "localhost";
		if (database == null) database = "enwiki";
		if (port == null) port = "";
		else port = ":" + port;
		if (charset == null) charset = "";
		else charset = "&useUnicode=true&characterEncoding=" + charset + "&characterSetResults=" + charset;
		if (collate == null) collate = "";
		else collate = "&connectionCollation=" + collate;
		if (embedded == null) {
			connection = DriverManager.getConnection(String.format("jdbc:mysql://%s%s/%s?user=%s&password=%s%s%s", host, port, database, user, password, charset, collate));
		}
		else {
			connection = DriverManager.getConnection(String.format("jdbc:mysql:mxj://%s%s/%s?user=%s&password=%s%s%s&%s", host, port, database, user, password, charset, collate, embedded));
		}
		connection.setAutoCommit(true);
	}

	public MySQLDatabase() {
	}
}
