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
import java.sql.Types;
import javatools.database.SQLType;

/**
 * This class replaces Fabian's OracleDatabase by implementing a newer version of Database class.
 * It supports new features of PostgresSQL server
 * 
 * @author tuan
 *
 */
public class PostgresDatabase extends Database {
	
	
	/** Holds the default schema*/
	  protected String schema=null;
	  
	  /** Constructs a non-functional OracleDatabase for use of getSQLType*/
	  public PostgresDatabase() {
	    java2SQL.put(String.class,varchar);
	    type2SQL.put(Types.VARCHAR,varchar);    
	  }
	  
	  /** Constructs a new Database from a user, a password and a host
	   * @throws ClassNotFoundException 
	   * @throws IllegalAccessException 
	   * @throws InstantiationException 
	   * @throws SQLException */
	  public PostgresDatabase(String user, String password, String database, String host, String port, boolean useSSL) throws SQLException  {
	    this();
	    if(password==null) password="";
	    if(host==null) host="localhost";
	    if(port==null) port="5432";
	    Driver driver;
		try {
			driver = (Driver)Class.forName("org.postgresql.Driver").newInstance();
		} 
		catch (InstantiationException e) {
			throw new SQLException(e);
		} 
		catch (IllegalAccessException e) {
			throw new SQLException(e);
		} 
		catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
	    DriverManager.registerDriver( driver );
	    String url = "jdbc:postgresql://"+host+":"+port+(database==null?"":"/"+database)+(useSSL?"?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory":"");
	    connection = DriverManager.getConnection(url, user, password);
	    connection.setAutoCommit( true );
	    description="Postgres database for "+user+" at "+host+":"+port+", database "+database+" schema "+schema;
	  }  
	  
	  public PostgresDatabase(String user, String password, String database, String host, String port) throws SQLException {
	    this(user,password,database,host,port,false);
	  }

	  /** Constructs a new Database from a user, a password and a host*/
	  public PostgresDatabase(String user, String password, String database, String host, String port,String schema) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException  {
	    this(user,password,database,host,port,false);
	    setSchema(schema);
	  }
	  /** Sets the default schema*/
	  public void setSchema(String s) throws SQLException {
	    executeUpdate("SET search_path TO "+s+", public");
	    schema=s;
	    description=description.substring(0,description.lastIndexOf(' '))+" "+schema;
	  }
	  
	  public static class Varchar extends SQLType.ANSIvarchar {
	    public Varchar(int size) {
	      super(size);
	    }  
	    public Varchar() {
	      super();
	    } 
	    public String toString() {
	      return("VARCHAR("+scale+")");
	    }
	    public String format(Object o) {
	      String s=o.toString().replace("'", "''").replace("\\", "\\\\");
	      if(s.length()>scale) s=s.substring(0,scale);
	      return("'"+s+"'");
	    } 
	  }
	  public static Varchar varchar=new Varchar();
}
