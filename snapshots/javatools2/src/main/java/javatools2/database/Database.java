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

import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.list.array.TShortArrayList;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javatools.database.ResultIterator;

/**
 * This class extends Fabian's wrapping methods with the following new features / contributions:<br>
 * <ul>
 * <li>Primitive direct wrapping without incurring from boxing/unboxing overhead </li>
 * <li>Query with parameters </li>
 * </ul>
 * @author tuan
 *
 */
public class Database extends javatools.database.Database {

	/** The timeout for connection valid */
	private static final int TIME_OUT = 3;

	/** Returns the results for a query as a ResultIterator. This is an 
	 * extension of corresponding query method in Fabian's Database class 
	 * with a single string parameter */
	public <T> ResultIterator<T> query(CharSequence sql, ResultIterator.ResultWrapper<T> rc, String... param) throws SQLException {
		return (new ResultIterator<T>(query(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, param), rc));
	}
	
	/** Returns the results for a query as a ResultIterator. This is an 
	 * extension of corresponding query method in Fabian's Database class 
	 * with a byte array parameter */
	public <T> ResultIterator<T> query(CharSequence sql, ResultIterator.ResultWrapper<T> rc, byte[] param) throws SQLException {
		return (new ResultIterator<T>(query(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, param), rc));
	}

	/** Returns the results for a query as a ResultIterator. This is an 
	 * extension of corresponding query method in Fabian's Database class 
	 * with a single integer parameter */
	public <T> ResultIterator<T> query(CharSequence sql, ResultIterator.ResultWrapper<T> rc, int... param) throws SQLException {
		return (new ResultIterator<T>(query(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, param), rc));
	}

	/** Returns a single value (or null). This is an extension of corresponding 
	 * query method in Fabian's Database class with a single string parameter */
	public <T> T queryValue(CharSequence sql, ResultIterator.ResultWrapper<T> rc, String... param) throws SQLException {
		ResultIterator<T> results = new ResultIterator<T>(query(sql, param), rc);
		T result = results.nextOrNull();
		results.close();
		//close();
		return (result);
	}

	/** Returns a single value (or null). This is an extension of corresponding 
	 * query method in Fabian's Database class with a single integer parameter */
	public <T> T queryValue(CharSequence sql, int param, ResultIterator.ResultWrapper<T> rc) throws SQLException {
		ResultIterator<T> results = new ResultIterator<T>(query(sql, param), rc);
		T result = results.nextOrNull();
		results.close();
		//close();
		return (result);
	}
	
	/** Returns a single value (or null). This is an extension of corresponding 
	 * query method in Fabian's Database class with a single integer parameter */
	public <T> T queryValue(CharSequence sql, ResultIterator.ResultWrapper<T> rc, int... param) throws SQLException {
		ResultIterator<T> results = new ResultIterator<T>(query(sql, param), rc);
		T result = results.nextOrNull();
		results.close();
		//close();
		return (result);
	}

	/**
	 * This is an int-primitive version of queryValue without parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */
	public int queryIntValue(CharSequence sql) throws SQLException {
		ResultSet rs = query(sql);
		if (rs.next()) {
			int i = rs.getInt(1);
			close(rs);
			//close();
			return i;
		}
		else {
			close(rs);
			return Integer.MAX_VALUE;
		}
	}

	/**
	 * This is an int-primitive version of queryValue with parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */
	public int queryIntValue(CharSequence sql, String... param) throws SQLException {
		ResultSet rs = query(sql, param);
		if (rs.next()) {
			int i = rs.getInt(1);
			close(rs.getStatement());
			close(rs);
			rs.close();
			//close();
			return i;
		}
		else {
			close(rs.getStatement());
			close(rs);
			rs.close();
			return Integer.MAX_VALUE;
		}
	}
	
	/**
	 * This is an varbinary version of queryValue with parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */
	public int queryIntValue(CharSequence sql, byte[] param) throws SQLException {
		ResultSet rs = query(sql, param);
		if (rs.next()) {
			int i = rs.getInt(1);
			close(rs.getStatement());
			close(rs);
			//close();
			return i;
		}
		else {
			close(rs.getStatement());
			close(rs);
			return Integer.MAX_VALUE;
		}
	}
		
	/**
	 * This is an int-primitive version of queryValue with parameters. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */
	public int queryIntValue(CharSequence sql, int... param) throws SQLException {
		ResultSet rs = query(sql, param);
		if (rs.next()) {
			int i = rs.getInt(1);
			close(rs.getStatement());
			close(rs);
			//close();
			return i;
		}
		else {
			close(rs);
			return Integer.MAX_VALUE;
		}
	}

	/**
	 * This is an float-primitive version of queryValue with parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */
	public float queryFloatValue(CharSequence sql, String param) throws SQLException {
		ResultSet rs = query(sql, param);
		if (rs.next()) {
			float f = rs.getFloat(1);
			close(rs);
			//close();
			return f;
		}
		else {
			close(rs);
			return Float.MAX_VALUE;
		}
	}

	/**
	 * This is an float-primitive version of queryValue without parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */
	public float queryFloatValue(CharSequence sql) throws SQLException {
		ResultSet rs = query(sql);
		if (rs.next()) {
			float f = rs.getFloat(1);
			close(rs);
			//close();
			return f;
		}
		else {
			close(rs);
			return Float.MAX_VALUE;
		}
	}

	/**
	 * This is an double-primitive version of queryValue with parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */	
	public double queryDoubleValue(CharSequence sql, String param) throws SQLException {
		ResultSet rs = query(sql, param);
		if (rs.next()) {
			double d = rs.getDouble(1);
			close(rs);
			//close();
			return d;
		}
		else {
			close(rs);
			return Double.MAX_VALUE;
		}
	}

	/**
	 * This is an double-primitive version of queryValue without parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */
	public double queryDoubleValue(CharSequence sql) throws SQLException {
		ResultSet rs = query(sql);
		if (rs.next()) {
			double d = rs.getDouble(1);
			close(rs);
			//close();
			return d;
		}
		else {
			close(rs);
			return Double.MAX_VALUE;
		}
	}

	/**
	 * This is an long-primitive version of queryValue with parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */
	public long queryLongValue(CharSequence sql, String param) throws SQLException {
		ResultSet rs = query(sql, param);
		if (rs.next()) {
			long l = rs.getLong(1);
			close(rs);
			//close();
			return l;
		}
		else {
			close(rs);
			return Long.MAX_VALUE;
		}
	}

	/**
	 * This is an long-primitive version of queryValue without parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */
	public long queryLongValue(CharSequence sql) throws SQLException {
		ResultSet rs = query(sql);
		if (rs.next()) {
			long l = rs.getLong(1);
			close(rs);
			//close();
			return l;
		}
		else {
			close(rs);
			return Long.MAX_VALUE;
		}
	}

	/**
	 * This is an short-primitive version of queryValue with parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */
	public short queryShortValue(CharSequence sql, String param) throws SQLException {
		ResultSet rs = query(sql, param);
		if (rs.next()) {
			short s = rs.getShort(1);
			close(rs);
			//close();
			return s;
		}
		else {
			close(rs);
			return Short.MAX_VALUE;
		}
	}

	/**
	 * This is an short-primitive version of queryValue without parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */
	public short queryShortValue(CharSequence sql) throws SQLException {
		ResultSet rs = query(sql);
		if (rs.next()) {
			short s = rs.getShort(1);
			close(rs);
			//close();
			return s;
		}
		else {
			close(rs);
			return Short.MAX_VALUE;
		}
	}

	/**
	 * This is an byte-primitive version of queryValue with parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */

	public byte queryByteValue(CharSequence sql, String param) throws SQLException {
		ResultSet rs = query(sql, param);
		if (rs.next()) {
			byte b = rs.getByte(1);
			close(rs);
			//close();
			return b;
		}
		else {
			close(rs);
			return Byte.MAX_VALUE;
		}
	}

	/**
	 * This is an byte-primitive version of queryValue without parameter. It returns 
	 * the value of a resultset if it has data, Integer.MAX_VALUE if 
	 * the result set doesn't have data
	 * 
	 */
	public byte queryByteValue(CharSequence sql) throws SQLException {
		ResultSet rs = query(sql);
		if (rs.next()) {
			byte b = rs.getByte(1);
			close(rs);
			//close();
			return b;
		}
		else {
			close(rs);
			return Byte.MAX_VALUE;
		}
	}

	/**
	 * Return the list of primitive int, or null if the resultset is empty
	 * 
	 */
	public int[] queryIntsValue(CharSequence sql) throws SQLException {
		TIntArrayList list = new TIntArrayList();
		ResultSet rs = query(sql);

		while (rs.next()) {
			list.add(rs.getInt(1));
		}
		close(rs);
		//close();
		return (list.size() == 0) ? null : list.toArray();
	}
	
	/**
	 * Return the list of primitive int with parameters, or null if the resultset is empty
	 * 
	 */
	public int[] queryIntsValue(CharSequence sql, String... param) throws SQLException {
		TIntArrayList list = new TIntArrayList();
		ResultSet rs = query(sql, param);

		while (rs.next()) {
			list.add(rs.getInt(1));
		}
		close(rs);
		//close();
		return (list.size() == 0) ? null : list.toArray();
	}
	
	/**
	 * Return the list of primitive int with parameters, or null if the resultset is empty
	 * 
	 */
	public int[] queryIntsValue(CharSequence sql, byte[] param) throws SQLException {
		TIntArrayList list = new TIntArrayList();
		ResultSet rs = query(sql, param);

		while (rs.next()) {
			list.add(rs.getInt(1));
		}
		close(rs);
		//close();
		return (list.size() == 0) ? null : list.toArray();
	}
	
	/**
	 * Return the list of primitive int with parameters, or null if the resultset is empty
	 * Variant with integer arguments
	 * 
	 */
	public int[] queryIntsValue(CharSequence sql, int... param) throws SQLException {
		TIntArrayList list = new TIntArrayList();
		ResultSet rs = query(sql, param);

		while (rs.next()) {
			list.add(rs.getInt(1));
		}
		close(rs);
		//close();

		return (list.size() == 0) ? null : list.toArray();
	}

	/**
	 * Return the list of primitive double with parameters, or null if the resultset is empty
	 * 
	 */
	public double[] queryDoublesValue(CharSequence sql, String... param) throws SQLException {
		TDoubleArrayList list = new TDoubleArrayList();
		ResultSet rs = query(sql, param);

		while (rs.next()) {
			list.add(rs.getDouble(1));
		}
		close(rs);
		//close();

		return (list.size() == 0) ? null : list.toArray();
	}

	/**
	 * Return the list of primitive byte with parameters, or null if the resultset is empty
	 * 
	 */
	public byte[] queryBytesValue(CharSequence sql, String... param) throws SQLException {
		TByteArrayList list = new TByteArrayList();
		ResultSet rs = query(sql, param);

		while (rs.next()) {
			list.add(rs.getByte(1));
		}
		close(rs);
		//close();

		return (list.size() == 0) ? null : list.toArray();
	}

	/**
	 * Return the list of primitive float with parameters, or null if the resultset is empty
	 * 
	 */
	public float[] queryFloatsValue(CharSequence sql, String... param) throws SQLException {
		TFloatArrayList list = new TFloatArrayList();
		ResultSet rs = query(sql, param);

		while (rs.next()) {
			list.add(rs.getFloat(1));
		}
		close(rs);
		//close();

		return (list.size() == 0) ? null : list.toArray();
	}

	/**
	 * Return the list of primitive long with parameters, or null if the resultset is empty
	 * 
	 */
	public long[] queryLongsValue(CharSequence sql, String... param) throws SQLException {
		TLongArrayList list = new TLongArrayList();
		ResultSet rs = query(sql, param);

		while (rs.next()) {
			list.add(rs.getLong(1));
		}
		close(rs);
		//close();

		return (list.size() == 0) ? null : list.toArray();
	}

	/**
	 * Return the list of primitive short with parameters, or null if the resultset is empty
	 * 
	 */
	public short[] queryShortsValue(CharSequence sql, String... param) throws SQLException {
		TShortArrayList list = new TShortArrayList();
		ResultSet rs = query(sql, param);

		while (rs.next()) {
			list.add(rs.getShort(1));
		}
		close(rs);
		//close();

		return (list.size() == 0) ? null : list.toArray();
	}

	/**
	 * Returns the results for a query as a ResultSet with default type and
	 * concurrency (read comments!). The preferred way to execute a query is by
	 * the query(String, ResultWrapper) method, because it ensures that the
	 * statement is closed afterwards. If you use the query(String) method
	 * instead, be sure to call Database.close(ResultSet) on the result set,
	 * because this ensures that the underlying statement is closed. The preferred
	 * way to execute an update query (i.e. INSERT/DELETE/UPDATE) is via the
	 * executeUpdate method, because it does not create an open statement. If
	 * query(String) is called with an update query, this method calls
	 * executeUpdate automatically and returns null. This is an extension of 
	 * corresponding query method in Fabian's Database class with a number of string parameters. 
	 */
	public ResultSet query(CharSequence sql, String... param) throws SQLException {
		return (query(sql, resultSetType, resultSetConcurrency, param));
	}
	
	/**
	 * Returns the results for a query as a ResultSet with default type and
	 * concurrency (read comments!). The preferred way to execute a query is by
	 * the query(String, ResultWrapper) method, because it ensures that the
	 * statement is closed afterwards. If you use the query(String) method
	 * instead, be sure to call Database.close(ResultSet) on the result set,
	 * because this ensures that the underlying statement is closed. The preferred
	 * way to execute an update query (i.e. INSERT/DELETE/UPDATE) is via the
	 * executeUpdate method, because it does not create an open statement. If
	 * query(String) is called with an update query, this method calls
	 * executeUpdate automatically and returns null. This is an extension of 
	 * corresponding query method in Fabian's Database class with a varbinary parameter
	 */
	public ResultSet query(CharSequence sql, byte[] param) throws SQLException {
		return (query(sql, resultSetType, resultSetConcurrency, param));
	}

	/**
	 * Returns the results for a query as a ResultSet with default type and
	 * concurrency (read comments!). The preferred way to execute a query is by
	 * the query(String, ResultWrapper) method, because it ensures that the
	 * statement is closed afterwards. If you use the query(String) method
	 * instead, be sure to call Database.close(ResultSet) on the result set,
	 * because this ensures that the underlying statement is closed. The preferred
	 * way to execute an update query (i.e. INSERT/DELETE/UPDATE) is via the
	 * executeUpdate method, because it does not create an open statement. If
	 * query(String) is called with an update query, this method calls
	 * executeUpdate automatically and returns null. This is an extension of 
	 * corresponding query method in Fabian's Database class with a number of integer parameters. 
	 */
	public ResultSet query(CharSequence sql, int... param) throws SQLException {
		return (query(sql, resultSetType, resultSetConcurrency, param));
	}

	/**
	 * Returns the results for a query as a ResultSet with given type and
	 * concurrency.This is an extension of a corresponding query method in 
	 * Fabian's Database class with a number of string parameters
	 */
	public ResultSet query(CharSequence sqlcs, int resultSetType, int resultSetConcurrency, String... param) throws SQLException {
		String sql = prepareQuery(sqlcs.toString());
		if (sql.toUpperCase().startsWith("INSERT") || sql.toUpperCase().startsWith("UPDATE") || sql.toUpperCase().startsWith("DELETE")
				|| sql.toUpperCase().startsWith("CREATE") || sql.toUpperCase().startsWith("DROP") || sql.toUpperCase().startsWith("ALTER")) {
			executeUpdate(sql);
			return (null);
		}
		try {
			PreparedStatement ps = connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
			int n = param.length;
			for (int i = 1; i <= n; i++)
				ps.setString(i, param[i - 1]);
			return ps.executeQuery();
		} catch (SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Returns the results for a query as a ResultSet with given type and
	 * concurrency.This is an extension of a corresponding query method in 
	 * Fabian's Database class with an arbitrary parameter
	 */
	public ResultSet query(CharSequence sqlcs, int resultSetType, int resultSetConcurrency, Object param, int type) throws SQLException {
		String sql = prepareQuery(sqlcs.toString());
		if (sql.toUpperCase().startsWith("INSERT") || sql.toUpperCase().startsWith("UPDATE") || sql.toUpperCase().startsWith("DELETE")
				|| sql.toUpperCase().startsWith("CREATE") || sql.toUpperCase().startsWith("DROP") || sql.toUpperCase().startsWith("ALTER")) {
			executeUpdate(sql);
			return (null);
		}
		try {
			PreparedStatement ps = connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
			ps.setObject(1, param, type);

			return ps.executeQuery();
		} catch (SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Returns the results for a query as a ResultSet with given type and
	 * concurrency.This is an extension of a corresponding query method in 
	 * Fabian's Database class with a varbinary parameter
	 */
	public ResultSet query(CharSequence sqlcs, int resultSetType, int resultSetConcurrency, byte[] param) throws SQLException {
		String sql = prepareQuery(sqlcs.toString());
		if (sql.toUpperCase().startsWith("INSERT") || sql.toUpperCase().startsWith("UPDATE") || sql.toUpperCase().startsWith("DELETE")
				|| sql.toUpperCase().startsWith("CREATE") || sql.toUpperCase().startsWith("DROP") || sql.toUpperCase().startsWith("ALTER")) {
			executeUpdate(sql);
			return (null);
		}
		try {
			PreparedStatement ps = connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
			ps.setBytes(1, param);

			return ps.executeQuery();
		} catch (SQLException e) {
			throw e;
		}
	}
	
	/**
	 * Returns the results for a query as a ResultSet with given type and
	 * concurrency.This is an extension of a corresponding query method in 
	 * Fabian's Database class with a number of integer parameters
	 */
	public ResultSet query(CharSequence sqlcs, int resultSetType, int resultSetConcurrency, int... param) throws SQLException {
		String sql = prepareQuery(sqlcs.toString());
		if (sql.toUpperCase().startsWith("INSERT") || sql.toUpperCase().startsWith("UPDATE") || sql.toUpperCase().startsWith("DELETE")
				|| sql.toUpperCase().startsWith("CREATE") || sql.toUpperCase().startsWith("DROP") || sql.toUpperCase().startsWith("ALTER")) {
			executeUpdate(sql);
			return (null);
		}
		try {
			PreparedStatement ps = connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
			int n = param.length;
			for (int i = 1; i <= n; i++)
				ps.setInt(i, param[i - 1]);
			return ps.executeQuery();
		} catch (SQLException e) {
			throw e;
		}
	}

	/** 
	 * Executes an SQL update query, returns the number of rows added/modified/deleted.  
	 * This is an extension of a corresponding query method in Fabian's Database class 
	 * with a number of string parameters */
	public int executeUpdate(CharSequence sqlcs, String... param ) throws SQLException {
		String sql = prepareQuery(sqlcs.toString());
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			int n = param.length;
			for (int i = 1; i <= n; i++)
				ps.setString(i, param[i - 1]);
			int result = ps.executeUpdate();
			close(ps);
			return (result);
		} catch (SQLException e) {
			throw new SQLException(sql + "\n" + e.getMessage());
		}
	}
	
	/** 
	 * Executes an SQL update query, returns the number of rows added/modified/deleted.  
	 * This is an extension of a corresponding query method in Fabian's Database class 
	 * with a number of integer parameters */
	public int executeUpdate(CharSequence sqlcs, int... param) throws SQLException {
		String sql = prepareQuery(sqlcs.toString());
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			int n = param.length;
			for (int i = 1; i <= n; i++)
				ps.setInt(i, param[i - 1]);
			int result = ps.executeUpdate();
			close(ps);
			return (result);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(sql + "\n" + e.getMessage());
		}
	}
	
	
	/** 
	 * Executes an SQL generic query  
	 * This is an extension of a corresponding query method in Fabian's Database class 
	 * with a number of integer parameters */
	public void execute(CharSequence sqlcs, int... param) throws SQLException {
		String sql = prepareQuery(sqlcs.toString());
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			int n = param.length;
			for (int i = 1; i <= n; i++)
				ps.setInt(i, param[i - 1]);
			ps.execute();
			close(ps);
		} 
		catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(sql + "\n" + e.getMessage());
		}
	}
	
	/** 
	 * Executes an SQL update query, returns the number of rows added/modified/deleted.  
	 * This is an extension of a corresponding query method in Fabian's Database class 
	 * with a number of string parameters */
	public void executeUpdate(CharSequence sqlcs, Value... param ) throws SQLException {
		String sql = prepareQuery(sqlcs.toString());
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			int n = param.length;
			for (int i = 1; i <= n; i++)
				ps.setObject(i, param[i - 1].getValue(), param[i - 1].getType());
			ps.execute();
			close(ps);
			//close();
		} 
		catch (SQLException e) {
			throw new SQLException(sql + "\n" + e.getMessage());
		}
	}
	
	/**
	 * Execute a generic query with return parameters (default type is Integer). This method gets around known bugs 
	 * in Oracle's DML statements handling (http://forums.oracle.com/forums/thread.jspa?threadID=943680)
	 * @throws SQLException 
	 */
	public int returnedExecute(CharSequence sqlcs, String returnParameter, int type) throws SQLException {
		 StringBuilder sql = new StringBuilder("{call ");
		 sql.append(prepareQuery(sqlcs.toString()));
		 sql.append("RETURNING ");
		 sql.append(returnParameter);
		 sql.append(" INTO ?}");
		    try {
		      CallableStatement s = connection.prepareCall(sql.toString());
		      s.registerOutParameter(1, type);
		      int result = s.executeUpdate();
		      close(s);
		      if (result > 0) return (s.getInt(1));
		      else throw new SQLException("SQL statement did not affect: " + sql);
		    } 
		    catch (SQLException e) {
		      throw new SQLException(sql + "\n" + e.getMessage());
		    }
	}
	
	/** This method is reserved. Each third party-compliant implementation of Database HAS to override this
	 * method */
	public int returnedExecute(CharSequence sqlcs, int... param) throws SQLException {
		return -1;
	}
	
	public boolean execute(CharSequence sqlcs) throws SQLException {
		String sql = prepareQuery(sqlcs.toString());
		try {
			Statement s = connection.createStatement();
		      boolean result = s.execute(sql);
		      close(s);
		      return (result);
		}
		catch (SQLException e) {
		     throw new SQLException(sql + "\n" + e.getMessage());
		}
	}
	
	/**
	 * Execute a generic query with return parameters (default type is Integer). This method gets around known bugs 
	 * in Oracle's DML statements handling (http://forums.oracle.com/forums/thread.jspa?threadID=943680)
	 * @throws SQLException 
	 */
	public int returnedExecute(CharSequence sqlcs, int type, String returnParameter, int... param) throws SQLException {
		 //StringBuilder sql = new StringBuilder("{call ");
		StringBuilder sql = new StringBuilder();
		 sql.append(prepareQuery(sqlcs.toString()));
		 sql.append("RETURNING ");
		 sql.append(returnParameter);
		 //sql.append(" INTO ?}");
		 sql.append(" INTO ?;");
		    try {
		      CallableStatement s = connection.prepareCall(sql.toString());
		      int n = param.length;
				for (int i = 1; i <= n; i++)
					s.setInt(i, param[i - 1]);
		      s.registerOutParameter(n + 1, type);
		      int result = s.executeUpdate();
		      close(s);
		      if (result > 0) return (s.getInt(1));
		      else throw new SQLException("SQL statement did not affect: " + sql);
		    } catch (SQLException e) {
		      throw new SQLException(sql + "\n" + e.getMessage());
		    }
	}
	
	/**
	 * Return the Ansi SQL colum types of a table
	 * @param table
	 * @throws SQLException
	 */
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

	/**
	 * Returns the results for a query as a ResultSet with given type and
	 * concurrency.This is an extension of a corresponding query method in 
	 * Fabian's Database class with a single int parameter
	 */
	public ResultSet query(CharSequence sqlcs, int param, int resultSetType, int resultSetConcurrency) throws SQLException {
		String sql = prepareQuery(sqlcs.toString());
		if (sql.toUpperCase().startsWith("INSERT") || sql.toUpperCase().startsWith("UPDATE") || sql.toUpperCase().startsWith("DELETE")
				|| sql.toUpperCase().startsWith("CREATE") || sql.toUpperCase().startsWith("DROP") || sql.toUpperCase().startsWith("ALTER")) {
			executeUpdate(sql);
			return (null);
		}
		try {
			PreparedStatement ps = connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
			ps.setInt(1, param);
			return ps.executeQuery();
		} catch (SQLException e) {
			throw e;
		}
	}

	public boolean isConnectionClosed() throws SQLException {
		return getConnection().isClosed();
	}

	public boolean isConnectionActive() throws SQLException {
		return getConnection().isValid(TIME_OUT);	
	}
}
