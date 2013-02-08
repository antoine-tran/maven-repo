package javatools.database;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.Map;
/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).





The class MySQLDatabase implements the Database-interface for a
MySQL data base. Make sure that the file 
"mysql-connector-java-<i>version</i>-bin.jar" from the "MySQL Connector/J" 
(see the <A HREF=http://dev.mysql.com/downloads/ TARGET=_blank>MySQL-website</A>)
is in the classpath. When using Eclipse, add the file via Project 
->Properties ->JavaBuildPath ->Libraries ->ExternalJARFile.<BR>
Example:
<PRE>
     Database d=new MySQLDatabase("user","password","database");     
     d.queryColumn("SELECT foodname FROM food WHERE origin=\"Italy\"")
     -> [ "Pizza Romana", "Spaghetti alla Bolognese", "Saltimbocca"]
     Database.describe(d.query("SELECT * FROM food WHERE origin=\"Italy\"")
     -> foodname |origin  |calories |
        ------------------------------
        Pizza Rom|Italy   |10000    |
        Spaghetti|Italy   |8000     |
        Saltimboc|Italy   |8000     |        
</PRE>
 */
public class MySQLDatabase extends Database {

	/** Constructs a new MySQLDatabase from a user and a password,
	 * all other arguments may be null*/
	public MySQLDatabase(String user, String password, String database, String host, String port) 
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException  {
		this(user, password, database, host, port, null, null);
	}

	/** Constructs a new MySQLDatabase from a user and a password, all other arguments may be null
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException */
	public MySQLDatabase(String user, String password, String database, String host, String port, 
			String charset, String collate)  throws SQLException, InstantiationException, 
			IllegalAccessException, ClassNotFoundException {
		Driver 	driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
		DriverManager.registerDriver(driver);
		if (host == null) host = "localhost";
		if (database == null) database = "enwiki";
		if (port == null) port = "";
		else port = ":" + port;
		if (charset == null) charset = "";
		else charset = "&useUnicode=true&characterEncoding=" + charset + "&characterSetResults=" + charset;
		if (collate == null) collate = "";
		else collate = "&connectionCollation=" + collate;
		String connectionString = String.format("jdbc:mysql://%s%s/%s?user=%s&password=%s%s%s", 
				host, port, database, user, password, charset, collate);
		connection = DriverManager.getConnection(connectionString);
		connection.setAutoCommit( true );  
		description="MySQL database for "+user+" at "+host+":"+port+", database "+database;

		type2SQL.put(Types.REAL, SQLType.ansifloat);	  
		type2SQL.put(Types.BLOB,blob);
		type2SQL.put(-4,blob);

		//java2SQL.put(String.class,blob);
		java2SQL.put(String.class, ansivarcharbin);
		type2SQL.put(Types.VARCHAR, ansivarcharbin);
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
		
		description="MySQL database for "+user+" at "+host+":"+port+", database "+database;

		type2SQL.put(Types.REAL, SQLType.ansifloat);	  
		type2SQL.put(Types.BLOB,blob);
		type2SQL.put(-4,blob);

		//java2SQL.put(String.class,blob);
		java2SQL.put(String.class, ansivarcharbin);
		type2SQL.put(Types.VARCHAR, ansivarcharbin);
	}

	public MySQLDatabase() {
	}

	@Override
	public boolean jarAvailable() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public String format(Object o) {
		String s=o.toString().replace("'", "''").replace("\\", "\\\\");
		//if(s.length()>scale) s=s.substring(0,scale);

		return("'"+s+"'");
	} 

	public static class Blob extends SQLType.ANSIblob {
		public Blob(int size) {
			super(size);
		}  
		public Blob() {
			super();
		} 
		public String toString() {
			return("BLOB");
		}
		public String format(Object o) {
			String s=o.toString().replace("'", "\'").replace("\\", "\\\\");	      
			return("'"+s+"'");
		} 
	}
	public static Blob blob=new Blob();	

	// a VARCHAR BINARY TYPE, making sure we are caseSensitive in varchar fields (this can probably be avoided by choosing a case-sensitive collation (not sure))
	public static class ANSIvarcharBin extends SQLType {
		public ANSIvarcharBin(int size) {
			typeCode=Types.VARCHAR;
			scale=size;
		}  
		public ANSIvarcharBin() {
			this(255);
		}        
		public String format(Object o) {
			String s=o.toString().replace("'", "\\'");
			if(s.length()>scale) s=s.substring(0,scale);
			return("'"+s+"'");
		}
		public String toString() {
			return("VARCHAR("+scale+") BINARY");      
		}
	}
	public static ANSIvarcharBin ansivarcharbin=new ANSIvarcharBin();


	/** Locks a table in write mode, i.e. other db connections can only read the table, but not write to it 
	 * Be careful as to not run into deadlocks! 
	 * Especially do not try to lock tables independently in separate steps, lock all tables needed for some processing in one call*/
	public void lockTableWriteAccess(Map<String, String> tablesAndAliases) throws SQLException{
		StringBuilder sql=new StringBuilder("LOCK TABLES ");
		Iterator<String> it=tablesAndAliases.keySet().iterator();
		while(it.hasNext()){
			String table=it.next();
			sql.append(table).append(" ");
			if(tablesAndAliases.get(table)!=null)
				sql.append("AS ").append(tablesAndAliases.get(table));
			sql.append(" WRITE" );
			if(it.hasNext())
				sql.append(", ");
		}
		connection.createStatement().executeUpdate(sql.toString());
	}

	/** Locks a table in read mode, i.e. only this connection can read or write the table
	 *  Be careful as to not run into deadlocks! 
	 *  Especially do not try to lock tables independently in separate steps, lock all tables needed for some processing in one call*/
	public void lockTableReadAccess(Map<String, String> tablesAndAliases) throws SQLException{	  
		StringBuilder sql=new StringBuilder("LOCK TABLES ");
		Iterator<String> it=tablesAndAliases.keySet().iterator();
		while(it.hasNext()){
			String table=it.next();
			sql.append(table).append(" ");
			if(tablesAndAliases.get(table)!=null)
				sql.append("AS ").append(tablesAndAliases.get(table));
			sql.append(" WRITE" );
			if(it.hasNext())
				sql.append(", ");
		}
		connection.createStatement().executeUpdate(sql.toString());
	}

	/** releases all locks the connection holds, commits the current transaction and ends it 
	 * switches back to autocommit mode*/
	@Override
	public void releaseLocksAndEndTransaction() throws SQLException{
		connection.createStatement().executeUpdate("UNLOCK TABLES");
		endTransaction(true);
	}

	// ---------------------------------------------------------------------
	//           DB specific SQL variations of common functionality
	// ---------------------------------------------------------------------

	/** returns the database system specific expression for isnull functionality 
	 * i.e. isnull(a,b) returns b if a is null and a otherwise */
	@Override
	public String getSQLStmntIFNULL(String a, String b){
		return "IFNULL("+a+","+b+")";
	}


	/** 
	 * Produces an SQL fragment casting the given value to the given type   * 
	 */
	@Override
	public String cast(String value, String type){
		if(type.equals("INTEGER"))
			type="UNSIGNED";
		else if(type.equals("VARCHAR"))
			type="CHAR";
		StringBuilder sql=new StringBuilder("CAST(");
		sql.append(value).append(" AS ").append(type).append(")");
		return sql.toString();	   
	}


	/** 
	 * Produces an SQL fragment representing column properties for an autoincrementing integer column
	 * s.t. if used during table creation a column can declared to get by default an 
	 * integer value assigned according to an internal self-incrementing sequence counter
	 * Example:
	 * createTable("tableWithSingleAutoIncrementingIDColumn", "ID", autoincrementColumn()) 
	 */
	@Override
	public String autoincrementColumn(){
		return "int AUTO_INCREMENT";
	}


	public static void main(String[] args) throws Exception {
	}
}
