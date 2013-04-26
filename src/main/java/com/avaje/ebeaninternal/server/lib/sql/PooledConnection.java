package com.avaje.ebeaninternal.server.lib.sql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.avaje.ebeaninternal.jdbc.ConnectionDelegator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Is a connection that belongs to a DataSourcePool.
 * 
 * <p>
 * It is designed to be part of DataSourcePool. Closing the connection puts it
 * back into the pool.
 * </p>
 * 
 * <p>
 * It defaults autoCommit and Transaction Isolation to the defaults of the
 * DataSourcePool.
 * </p>
 * 
 * <p>
 * It has caching of Statements and PreparedStatements. Remembers the last
 * statement that was executed. Keeps statistics on how long it is in use.
 * </p>
 */
public class PooledConnection extends ConnectionDelegator
{

	private static final Logger logger = LoggerFactory.getLogger(PooledConnection.class);

	private static String IDLE_CONNECTION_ACCESSED_ERROR = "Pooled Connection has been accessed whilst idle in the pool, via method: ";

	/**
	 * Set when connection is idle in the pool. In general when in the pool the
	 * connection should not be modified.
	 */
	static final int STATUS_IDLE = 88;

	/**
	 * Set when connection given to client.
	 */
	static final int STATUS_ACTIVE = 89;

	/**
	 * Set when commit() or rollback() called.
	 */
	static final int STATUS_ENDED = 87;

	/**
	 * Name used to identify the PooledConnection for logging.
	 */
	final String name;

	/**
	 * The pool this connection belongs to.
	 */
	final DataSourcePool pool;

	/**
	 * The underlying connection.
	 */
	final Connection connection;

	/**
	 * The time this connection was created.
	 */
	final long creationTime;

	/**
	 * Cache of the PreparedStatements
	 */
	final PstmtCache pstmtCache;

	final Object pstmtMonitor = new Object();
	
	/**
	 * The status of the connection. IDLE, ACTIVE or ENDED.
	 */
	int status = STATUS_IDLE;

	/**
	 * Set this to true if the connection will be busy for a long time.
	 * <p>
	 * This means it should skip the suspected connection pool leak checking.
	 * </p>
	 */
	boolean longRunning;
	
	/**
	 * Flag to indicate that this connection had errors and should be checked to
	 * make sure it is okay.
	 */
	boolean hadErrors;

	/**
	 * The last start time. When the connection was given to a thread.
	 */
	long startUseTime;

	/**
	 * The last end time of this connection. This is to calculate the usage
	 * time.
	 */
	long lastUseTime;

	/**
	 * The last statement executed by this connection.
	 */
	String lastStatement;

	/**
	 * The number of hits against the preparedStatement cache.
	 */
	int pstmtHitCounter;

	/**
	 * The number of misses against the preparedStatement cache.
	 */
	int pstmtMissCounter;

	/**
	 * The non avaje method that created the connection.
	 */
	String createdByMethod;

	/**
	 * Used to find connection pool leaks.
	 */
	StackTraceElement[] stackTrace;

	int maxStackTrace;
	
	/**
	 * Slot position in the BusyConnectionBuffer.
	 */
	int slotId;
	
	/**
	 * Construct the connection that can refer back to the pool it belongs to.
	 * <p>
	 * close() will return the connection back to the pool , while
	 * closeDestroy() will close() the underlining connection properly.
	 * </p>
	 */
	public PooledConnection(DataSourcePool pool, int uniqueId, Connection connection) throws SQLException {
		super(connection);
		
		this.pool = pool;
		this.connection = connection;
		this.name = pool.getName() + "." + uniqueId;
		this.pstmtCache = new PstmtCache(name, pool.getPstmtCacheSize());
		this.maxStackTrace = pool.getMaxStackTraceSize();
		this.creationTime = System.currentTimeMillis();
		this.lastUseTime = creationTime;
	}

	/**
	 * For testing the pool without real connections.
	 */
	protected PooledConnection(String name) {
	    super(null);
	    this.name = name;
	    this.pool = null;
	    this.connection = null;
	    this.pstmtCache = null;
	    this.maxStackTrace = 0;
        this.creationTime = System.currentTimeMillis();
        this.lastUseTime = creationTime;
    }
	
	/**
     * Return the slot position in the busy buffer.
     */
	public int getSlotId() {
        return slotId;
    }

    /**
     * Set the slot position in the busy buffer.
     */
    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    /**
	 * Return the DataSourcePool that this connection belongs to.
	 */
	public DataSourcePool getDataSourcePool() {
		return pool;
	}

	/**
	 * Return the time the connection was created.
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * Return a string to identify the connection.
	 */
	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}
	
	public String getDescription() {
		return "name["+name+"] startTime["+getStartUseTime()+"] stmt["+getLastStatement()+"] createdBy["+getCreatedByMethod()+"]";
	}
		
	public String getStatistics() {
		return "name["+name+"] startTime["+getStartUseTime()+"] pstmtHits["+pstmtHitCounter+"] pstmtMiss["+pstmtMissCounter+"] "+pstmtCache.getDescription();
	}
	
	/**
	 * Return true if the connection should be treated as long running (skip connection pool leak check).
	 */
	public boolean isLongRunning() {
		return longRunning;
	}

	/**
	 * Set this to true if the connection is a long running connection and should skip the
	 * 'suspected connection pool leak' checking.
	 */
	public void setLongRunning(boolean longRunning) {
		this.longRunning = longRunning;
	}

	/**
	 * Close the connection fully NOT putting in back into the pool.
	 * <p>
	 * The logErrors parameter exists so that expected errors are not logged
	 * such as when the database is known to be down.
	 * </p>
	 * 
	 * @param logErrors
	 *            if false then don't log errors when closing
	 */
	public void closeConnectionFully(boolean logErrors) {

		String msg = "Closing Connection[" + getName() + "]" + " psReuse[" + pstmtHitCounter
				+ "] psCreate[" + pstmtMissCounter + "] psSize[" + pstmtCache.size() + "]";

		logger.debug(msg);

		try {
			if (connection.isClosed()) {
			  // Typically the JDBC Driver has its own JVM shutdown hook and already 
			  // closed the connections in our DataSource pool so making this DEBUG level
				logger.debug("Closing Connection[" + getName() + "] that is already closed?");
				return;
			}
		} catch (SQLException ex) {
			if (logErrors) {
				logger.error("Error when fully closing connection [" + getName() + "]", ex);
			}
		}

		try {
			Iterator<ExtendedPreparedStatement> psi = pstmtCache.values().iterator();
			while (psi.hasNext()) {
				ExtendedPreparedStatement ps = (ExtendedPreparedStatement) psi.next();
				ps.closeDestroy();
			}

		} catch (SQLException ex) {
			if (logErrors) {
				logger.warn("Error when closing connection Statements", ex);
			}
		}

		try {
			connection.close();
		} catch (SQLException ex) {
			if (logErrors) {
				logger.error("Error when fully closing connection [" + getName() + "]", ex);
			}
		}
	}

	/**
	 * A Least Recently used cache of PreparedStatements.
	 */
	public PstmtCache getPstmtCache() {
		return pstmtCache;
	}

	/**
	 * Creates a wrapper ExtendedStatement so that I can get the executed sql. I
	 * want to do this so that I can get the slowest query statments etc, and
	 * log that information.
	 */
	public Statement createStatement() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "createStatement()");
		}
		try {
			return connection.createStatement();
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public Statement createStatement(int resultSetType, int resultSetConcurreny)
			throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "createStatement()");
		}
		try {
			return connection.createStatement(resultSetType, resultSetConcurreny);

		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	/**
	 * Return a PreparedStatement back into the cache.
	 */
	protected void returnPreparedStatement(ExtendedPreparedStatement pstmt) {

		 synchronized (pstmtMonitor) {
			ExtendedPreparedStatement alreadyInCache = pstmtCache.get(pstmt.getCacheKey());

			if (alreadyInCache == null) {
				// add the returning prepared statement to the cache.
				// Note that the LRUCache will automatically close fully old unused
				// PStmts when the cache has hit its maximum size.
				pstmtCache.put(pstmt.getCacheKey(), pstmt);
				
			} else {
				try {
					// if a entry in the cache exists for the exact same SQL...
					// then remove it from the cache and close it fully.
					// Only having one PreparedStatement per unique SQL
					// statement
					pstmt.closeDestroy();

				} catch (SQLException e) {
					logger.error("Error closing Pstmt", e);
				}
			}
		}
	}

	/**
	 * This will try to use a cache of PreparedStatements.
	 */
	public PreparedStatement prepareStatement(String sql, int returnKeysFlag) throws SQLException {
		String cacheKey = sql + returnKeysFlag;
		return prepareStatement(sql, true, returnKeysFlag, cacheKey);
	}

	/**
	 * This will try to use a cache of PreparedStatements.
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return prepareStatement(sql, false, 0, sql);
	}

	/**
	 * This will try to use a cache of PreparedStatements.
	 */
	private PreparedStatement prepareStatement(String sql, boolean useFlag, int flag, String cacheKey) throws SQLException {
		
		if (status == STATUS_IDLE) {
			String m = IDLE_CONNECTION_ACCESSED_ERROR + "prepareStatement()";
			throw new SQLException(m);
		}
		try {
			synchronized (pstmtMonitor) {
				lastStatement = sql;
	
				// try to get a matching cached PStmt from the cache.
				ExtendedPreparedStatement pstmt = pstmtCache.remove(cacheKey);
	
				if (pstmt != null) {
					pstmtHitCounter++;
					return pstmt;
				}
	
				// create a new PreparedStatement
				pstmtMissCounter++;
				PreparedStatement actualPstmt;
				if (useFlag) {
					actualPstmt = connection.prepareStatement(sql, flag);
				} else {
					actualPstmt = connection.prepareStatement(sql);
				}
				return new ExtendedPreparedStatement(this, actualPstmt, sql, cacheKey);
			}

		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurreny)
			throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "prepareStatement()");
		}
		try {
			// no caching when creating PreparedStatements this way
			pstmtMissCounter++;
			lastStatement = sql;
			return connection.prepareStatement(sql, resultSetType, resultSetConcurreny);
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	/**
	 * Reset the connection for returning to the client. Resets the status,
	 * startUseTime and hadErrors.
	 */
	protected void resetForUse() {
		this.status = STATUS_ACTIVE;
		this.startUseTime = System.currentTimeMillis();
		this.createdByMethod = null;
		this.lastStatement = null;
		this.hadErrors = false;
		this.longRunning = false;
	}

	/**
	 * When an error occurs during use add it the connection.
	 * <p>
	 * Any PooledConnection that has an error is checked to make sure it works
	 * before it is placed back into the connection pool.
	 * </p>
	 */
	public void addError(Throwable e) {
		hadErrors = true;
	}

	/**
	 * Returns true if the connect threw any errors during use.
	 * <p>
	 * Connections with errors are testing to make sure they are still good
	 * before putting them back into the pool.
	 * </p>
	 */
	public boolean hadErrors() {
		return hadErrors;
	}

	/**
	 * close the connection putting it back into the connection pool.
	 * <p>
	 * Note that to ensure that the next transaction starts at the correct time
	 * a commit() or rollback() should be called. If neither has occured at this
	 * time then a rollback() is used (to end the transaction).
	 * </p>
	 * <p>
	 * To close the connection fully use closeConnectionFully().
	 * </p>
	 */
	public void close() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "close()");
		}

		if (hadErrors) {
			if (!pool.validateConnection(this)) {
				// the connection is BAD, close it and test the pool
				closeConnectionFully(false);
				pool.checkDataSource();
				return;
			}
		}

		try {
			// reset the autoCommit back if client code changed it
			if (connection.getAutoCommit() != pool.getAutoCommit()) {
				connection.setAutoCommit(pool.getAutoCommit());
			}
			// Generally resetting Isolation level seems expensive.
			// Hence using resetIsolationReadOnlyRequired flag
			// performance reasons.
			if (resetIsolationReadOnlyRequired) {
				resetIsolationReadOnly();
				resetIsolationReadOnlyRequired = false;
			}

			// the connection is assumed GOOD so put it back in the pool
			lastUseTime = System.currentTimeMillis();
			// connection.clearWarnings();
			status = STATUS_IDLE;
			pool.returnConnection(this);

		} catch (Exception ex) {
			// the connection is BAD, close it and test the pool
			closeConnectionFully(false);
			pool.checkDataSource();
		}
	}

	private void resetIsolationReadOnly() throws SQLException {
		// reset the transaction isolation if the client code changed it
		if (connection.getTransactionIsolation() != pool.getTransactionIsolation()) {
			connection.setTransactionIsolation(pool.getTransactionIsolation());
		}
		// reset readonly to false
		if (connection.isReadOnly()) {
			connection.setReadOnly(false);
		}
	}

	protected void finalize() throws Throwable {
		try {
			if (connection != null && !connection.isClosed()) {
				// connect leak?
				String msg = "Closing Connection[" + getName() + "] on finalize().";
				logger.warn(msg);
				closeConnectionFully(false);
			}
		} catch (Exception e) {
			logger.error(null, e);
		}
		super.finalize();
	}

	/**
	 * Return the time the connection was passed to the client code.
	 * <p>
	 * Used to detect busy connections that could be leaks.
	 * </p>
	 */
	public long getStartUseTime() {
		return startUseTime;
	}

	/**
	 * Returns the time the connection was last used.
	 * <p>
	 * Used to close connections that have been idle for some time. Typically 5
	 * minutes.
	 * </p>
	 */
	public long getLastUsedTime() {
		return lastUseTime;
	}

	/**
	 * Returns the last sql statement executed.
	 */
	public String getLastStatement() {
		return lastStatement;
	}

	/**
	 * Called by ExtendedStatement to trace the sql being executed.
	 * <p>
	 * Note with addBatch() this will not really work.
	 * </p>
	 */
	protected void setLastStatement(String lastStatement) {
		this.lastStatement = lastStatement;
		if (logger.isTraceEnabled()) {
			logger.trace(".setLastStatement[" + lastStatement + "]");
		}
	}

	boolean resetIsolationReadOnlyRequired = false;

	/**
	 * Also note the read only status needs to be reset when put back into the
	 * pool.
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		// A bit loose not checking for STATUS_IDLE
		// if (status == STATUS_IDLE) {
		// throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR +
		// "setReadOnly()");
		// }
		resetIsolationReadOnlyRequired = true;
		connection.setReadOnly(readOnly);
	}

	/**
	 * Also note the Isolation level needs to be reset when put back into the
	 * pool.
	 */
	public void setTransactionIsolation(int level) throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "setTransactionIsolation()");
		}
		try {
			resetIsolationReadOnlyRequired = true;
			connection.setTransactionIsolation(level);
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	//
	// 
	// Simple wrapper methods which pass a method call onto the acutal
	// connection object. These methods are safe-guarded to prevent use of
	// the methods whilst the connection is in the connection pool.
	//
	//
	public void clearWarnings() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "clearWarnings()");
		}
		connection.clearWarnings();
	}

	public void commit() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "commit()");
		}
		try {
			status = STATUS_ENDED;
			connection.commit();
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}
	
	public boolean getAutoCommit() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "getAutoCommit()");
		}
		return connection.getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "getCatalog()");
		}
		return connection.getCatalog();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "getMetaData()");
		}
		return connection.getMetaData();
	}

	public int getTransactionIsolation() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "getTransactionIsolation()");
		}
		return connection.getTransactionIsolation();
	}

	public Map<String,Class<?>> getTypeMap() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "getTypeMap()");
		}
		return connection.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "getWarnings()");
		}
		return connection.getWarnings();
	}

	public boolean isClosed() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "isClosed()");
		}
		return connection.isClosed();
	}

	public boolean isReadOnly() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "isReadOnly()");
		}
		return connection.isReadOnly();
	}

	public String nativeSQL(String sql) throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "nativeSQL()");
		}
		lastStatement = sql;
		return connection.nativeSQL(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "prepareCall()");
		}
		lastStatement = sql;
		return connection.prepareCall(sql);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurreny)
			throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "prepareCall()");
		}
		lastStatement = sql;
		return connection.prepareCall(sql, resultSetType, resultSetConcurreny);
	}

	public void rollback() throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "rollback()");
		}
		try {
			status = STATUS_ENDED;
			connection.rollback();
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "setAutoCommit()");
		}
		try {
			connection.setAutoCommit(autoCommit);
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public void setCatalog(String catalog) throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "setCatalog()");
		}
		connection.setCatalog(catalog);
	}

	public void setTypeMap(Map<String,Class<?>> map) throws SQLException {
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "setTypeMap()");
		}
		connection.setTypeMap(map);
	}

	public Savepoint setSavepoint() throws SQLException {
		try {
			return connection.setSavepoint();
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public Savepoint setSavepoint(String savepointName) throws SQLException {
		try {
			return connection.setSavepoint(savepointName);
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public void rollback(Savepoint sp) throws SQLException {
		try {
			connection.rollback(sp);
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public void releaseSavepoint(Savepoint sp) throws SQLException {
		try {
			connection.releaseSavepoint(sp);
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public void setHoldability(int i) throws SQLException {
		try {
			connection.setHoldability(i);
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public int getHoldability() throws SQLException {
		try {
			return connection.getHoldability();
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public Statement createStatement(int i, int x, int y) throws SQLException {
		try {
			return connection.createStatement(i, x, y);
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public PreparedStatement prepareStatement(String s, int i, int x, int y) throws SQLException {
		try {
			return connection.prepareStatement(s, i, x, y);
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public PreparedStatement prepareStatement(String s, int[] i) throws SQLException {
		try {
			return connection.prepareStatement(s, i);
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public PreparedStatement prepareStatement(String s, String[] s2) throws SQLException {
		try {
			return connection.prepareStatement(s, s2);
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	public CallableStatement prepareCall(String s, int i, int x, int y) throws SQLException {
		try {
			return connection.prepareCall(s, i, x, y);
		} catch (SQLException ex) {
			addError(ex);
			throw ex;
		}
	}

	/**
	 * Returns the method that created the connection.
	 * <p>
	 * Used to help finding connection pool leaks.
	 * </p>
	 */
	public String getCreatedByMethod() {
		if (createdByMethod != null) {
			return createdByMethod;
		}
		if (stackTrace == null) {
			return null;
		}

		for (int j = 0; j < stackTrace.length; j++) {
			String methodLine = stackTrace[j].toString();
			if (skipElement(methodLine)) {
                // ignore these methods...
			} else {
				createdByMethod = methodLine;
				return createdByMethod;
			}
		}

		return null;
	}

	private boolean skipElement(String methodLine) {
	    if (methodLine.startsWith("java.lang.")) {
            return true;
	    } else if (methodLine.startsWith("java.util.")) {
	        return true;
        } else if (methodLine.startsWith("com.avaje.ebeaninternal.server.query.CallableQuery.<init>")) {
            // creating connection on future...
            return true;
        } else if (methodLine.startsWith("com.avaje.ebeaninternal.server.query.Callable")) {
            // it is a future task being executed...
            return false;
        } else if (methodLine.startsWith("com.avaje.ebeaninternal")) {
            return true;
        } else {
            return false;
        }
	}
	
	/**
	 * Set the stack trace to help find connection pool leaks.
	 */
	protected void setStackTrace(StackTraceElement[] stackTrace) {
		this.stackTrace = stackTrace;
	}

	/**
	 * Return the full stack trace that got the connection from the pool. You
	 * could use this if getCreatedByMethod() doesn't work for you.
	 */
	public StackTraceElement[] getStackTrace() {
	    
	    if (stackTrace == null){
	        return null;
	    } 
	    
	    // filter off the top of the stack that we are not interested in
        ArrayList<StackTraceElement> filteredList = new ArrayList<StackTraceElement>();
        boolean include = false;
        for (int i = 0; i < stackTrace.length; i++) {
            if (!include && !skipElement(stackTrace[i].toString())){
                include = true;
            }
            if (include && filteredList.size() < maxStackTrace){
                filteredList.add(stackTrace[i]);
            }
        }
        return filteredList.toArray(new StackTraceElement[filteredList.size()]);
	    	    
	}

}
