package com.avaje.ebeaninternal.server.lib.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

import com.avaje.ebeaninternal.jdbc.PreparedStatementDelegator;

/**
 * Implements the Statement methods for ExtendedPreparedStatement.
 * <p>
 * PreparedStatements should always be used and the intention is that there
 * should be no use of Statement at all. The implementation here is generally
 * for the case where someone uses the Statement api on an ExtendedPreparedStatement.
 * </p>
 */
public abstract class ExtendedStatement extends PreparedStatementDelegator
{

	/**
	 * The pooled connection this Statement belongs to.
	 */
    protected final PooledConnection pooledConnection;

	/**
	 * The underlying Statement that this object wraps.
	 */
	protected final PreparedStatement pstmt;

	/**
	 * Create the ExtendedStatement for a given pooledConnection.
	 */
	public ExtendedStatement(PooledConnection pooledConnection, PreparedStatement pstmt) {
		super(pstmt);

		this.pooledConnection = pooledConnection;
		this.pstmt = pstmt;
	}

	/**
	 * Put the statement back into the statement cache.
	 */
	public abstract void close() throws SQLException;

	/**
	 * Return the underlying connection.
	 */
	public Connection getConnection() throws SQLException {
		try {
			return pstmt.getConnection();
		} catch (SQLException e) {
			pooledConnection.addError(e);
			throw e;
		}
	}

	/**
	 * Add the sql for batch execution.
	 */
	public void addBatch(String sql) throws SQLException {
		try {
			pooledConnection.setLastStatement(sql);
			pstmt.addBatch(sql);
		} catch (SQLException e) {
			pooledConnection.addError(e);
			throw e;
		}
	}

	/**
	 * Execute the sql.
	 */
	public boolean execute(String sql) throws SQLException {
		try {
			pooledConnection.setLastStatement(sql);
			return pstmt.execute(sql);
		} catch (SQLException e) {
			pooledConnection.addError(e);
			throw e;
		}
	}

	/**
	 * Execute the query.
	 */
	public ResultSet executeQuery(String sql) throws SQLException {
		try {
			pooledConnection.setLastStatement(sql);
			return pstmt.executeQuery(sql);
		} catch (SQLException e) {
			pooledConnection.addError(e);
			throw e;
		}
	}

	/**
	 * Execute the dml sql.
	 */
	public int executeUpdate(String sql) throws SQLException {
		try {
			pooledConnection.setLastStatement(sql);
			return pstmt.executeUpdate(sql);
		} catch (SQLException e) {
			pooledConnection.addError(e);
			throw e;
		}
	}

	/**
	 * Standard Statement method call. 
	 */
	public int[] executeBatch() throws SQLException {
		return pstmt.executeBatch();
	}

	/**
	 * Standard Statement method call. 
	 */
	public void cancel() throws SQLException {
		pstmt.cancel();
	}

	/**
	 * Standard Statement method call. 
	 */
	public void clearBatch() throws SQLException {
		pstmt.clearBatch();
	}

	/**
	 * Standard Statement method call. 
	 */
	public void clearWarnings() throws SQLException {
		pstmt.clearWarnings();
	}

	/**
	 * Standard Statement method call. 
	 */
	public int getFetchDirection() throws SQLException {
		return pstmt.getFetchDirection();
	}

	/**
	 * Standard Statement method call. 
	 */
	public int getFetchSize() throws SQLException {
		return pstmt.getFetchSize();
	}

	/**
	 * Standard Statement method call. 
	 */
	public int getMaxFieldSize() throws SQLException {
		return pstmt.getMaxFieldSize();
	}

	/**
	 * Standard Statement method call. 
	 */
	public int getMaxRows() throws SQLException {
		return pstmt.getMaxRows();
	}	
	
	/**
	 * Standard Statement method call. 
	 */
	public boolean getMoreResults() throws SQLException {
		return pstmt.getMoreResults();
	}

	/**
	 * Standard Statement method call. 
	 */
	public int getQueryTimeout() throws SQLException {
		return pstmt.getQueryTimeout();
	}

	/**
	 * Standard Statement method call. 
	 */
	public ResultSet getResultSet() throws SQLException {
		return pstmt.getResultSet();
	}

	/**
	 * Standard Statement method call. 
	 */
	public int getResultSetConcurrency() throws SQLException {
		return pstmt.getResultSetConcurrency();
	}
	
	/**
	 * Standard Statement method call. 
	 */
	public int getResultSetType() throws SQLException {
		return pstmt.getResultSetType();
	}

	/**
	 * Standard Statement method call. 
	 */
	public int getUpdateCount() throws SQLException {
		return pstmt.getUpdateCount();
	}

	/**
	 * Standard Statement method call. 
	 */
	public SQLWarning getWarnings() throws SQLException {
		return pstmt.getWarnings();
	}

	/**
	 * Standard Statement method call. 
	 */
	public void setCursorName(String name) throws SQLException {
		pstmt.setCursorName(name);
	}

	/**
	 * Standard Statement method call. 
	 */
	public void setEscapeProcessing(boolean enable) throws SQLException {
		pstmt.setEscapeProcessing(enable);
	}

	/**
	 * Standard Statement method call. 
	 */
	public void setFetchDirection(int direction) throws SQLException {
		pstmt.setFetchDirection(direction);
	}

	/**
	 * Standard Statement method call. 
	 */
	public void setFetchSize(int rows) throws SQLException {
		pstmt.setFetchSize(rows);
	}

	/**
	 * Standard Statement method call. 
	 */
	public void setMaxFieldSize(int max) throws SQLException {
		pstmt.setMaxFieldSize(max);
	}

	/**
	 * Standard Statement method call. 
	 */
	public void setMaxRows(int max) throws SQLException {
		pstmt.setMaxRows(max);
	}

	/**
	 * Standard Statement method call. 
	 */
	public void setQueryTimeout(int seconds) throws SQLException {
		pstmt.setQueryTimeout(seconds);
	}

	/**
	 * Standard Statement method call. 
	 */
	public boolean getMoreResults(int i) throws SQLException {
		return pstmt.getMoreResults(i);
	}

	/**
	 * Standard Statement method call. 
	 */
	public ResultSet getGeneratedKeys() throws SQLException {
		return pstmt.getGeneratedKeys();
	}

	/**
	 * Standard Statement method call. 
	 */
	public int executeUpdate(String s, int i) throws SQLException {
		return pstmt.executeUpdate(s, i);
	}

	/**
	 * Standard Statement method call. 
	 */
	public int executeUpdate(String s, int[] i) throws SQLException {
		return pstmt.executeUpdate(s, i);
	}

	/**
	 * Standard Statement method call. 
	 */
	public int executeUpdate(String s, String[] i) throws SQLException {
		return pstmt.executeUpdate(s, i);
	}

	/**
	 * Standard Statement method call. 
	 */
	public boolean execute(String s, int i) throws SQLException {
		return pstmt.execute(s, i);
	}

	/**
	 * Standard Statement method call. 
	 */
	public boolean execute(String s, int[] i) throws SQLException {
		return pstmt.execute(s, i);
	}

	/**
	 * Standard Statement method call. 
	 */
	public boolean execute(String s, String[] i) throws SQLException {
		return pstmt.execute(s, i);
	}

	/**
	 * Standard Statement method call. 
	 */
	public int getResultSetHoldability() throws SQLException {
		return pstmt.getResultSetHoldability();
	}

}
