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
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebeaninternal.jdbc.ConnectionDelegator;

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
public class PooledConnection extends ConnectionDelegator {

	private static final Logger logger = LoggerFactory.getLogger(PooledConnection.class);

	private static final String IDLE_CONNECTION_ACCESSED_ERROR = "Pooled Connection has been accessed whilst idle in the pool, via method: ";

	/**
	 * Marker for when connection is closed due to exceeding the max allowed age.
	 */
	private static final String REASON_MAXAGE = "maxAge";
	
	/**
	 * Marker for when connection is closed due to exceeding the max inactive time.
	 */
	private static final String REASON_IDLE = "idleTime";
	
	/**
	 * Marker for when the connection is closed due to a reset.
	 */
  private static final String REASON_RESET = "reset";
	
	/**
	 * Set when connection is idle in the pool. In general when in the pool the
	 * connection should not be modified.
	 */
	private static final int STATUS_IDLE = 88;

	/**
	 * Set when connection given to client.
	 */
	private static final int STATUS_ACTIVE = 89;

	/**
	 * Set when commit() or rollback() called.
	 */
	private static final int STATUS_ENDED = 87;

	/**
	 * Name used to identify the PooledConnection for logging.
	 */
	private final String name;

	/**
	 * The pool this connection belongs to.
	 */
	private final DataSourcePool pool;

	/**
	 * The underlying connection.
	 */
	private final Connection connection;

	/**
	 * The time this connection was created.
	 */
	private final long creationTime;

	/**
	 * Cache of the PreparedStatements
	 */
	private final PstmtCache pstmtCache;

	private final Object pstmtMonitor = new Object();
	
	/**
	 * Helper for statistics collection. 
	 */
	private final PooledConnectionStatistics stats = new PooledConnectionStatistics();

	/**
	 * The status of the connection. IDLE, ACTIVE or ENDED.
	 */
	private int status = STATUS_IDLE;

	/**
	 * The reason for a connection closing.
	 */
	private String closeReason;
	
	/**
	 * Set this to true if the connection will be busy for a long time.
	 * <p>
	 * This means it should skip the suspected connection pool leak checking.
	 * </p>
	 */
	private boolean longRunning;
	
	/**
	 * Flag to indicate that this connection had errors and should be checked to
	 * make sure it is okay.
	 */
	private boolean hadErrors;

	/**
	 * The last start time. When the connection was given to a thread.
	 */
	private long startUseTime;

	/**
	 * The last end time of this connection. This is to calculate the usage
	 * time.
	 */
	private long lastUseTime;
	
	private long exeStartNanos;
	
	/**
	 * The last statement executed by this connection.
	 */
	private String lastStatement;

	/**
	 * The non avaje method that created the connection.
	 */
	private String createdByMethod;

	/**
	 * Used to find connection pool leaks.
	 */
	private StackTraceElement[] stackTrace;

	private int maxStackTrace;
	
	/**
	 * Slot position in the BusyConnectionBuffer.
	 */
	private int slotId;

	private boolean resetIsolationReadOnlyRequired;

	
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

	public String getNameSlot() {
    return name+":"+slotId;
  }
 
	public String toString() {
		return getDescription();
	}
	
	public long getBusySeconds() {
    return (System.currentTimeMillis() - startUseTime)/1000;
	}
	
	public String getDescription() {
		return "name["+name+"] slot["+slotId+"] startTime["+getStartUseTime()+"] busySeconds["+getBusySeconds()+"] createdBy["+getCreatedByMethod()+"] stmt["+getLastStatement()+"]";
	}

	public String getFullDescription() {
    return "name["+name+"] slot["+slotId+"] startTime["+getStartUseTime()+"] busySeconds["+getBusySeconds()+"] stackTrace["+getStackTraceAsString()+"] stmt["+getLastStatement()+"]";
  }

	public String getPstmtStatistics() {
		return "name["+name+"] startTime["+getStartUseTime()+"] "+pstmtCache.getDescription();
	}
	
	public PooledConnectionStatistics getStatistics() {
	  return stats;
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

	  if (pool != null) {
	    // allow collection of load statistics 
	    pool.reportClosingConnection(this);
	  }
	  
	  if (logger.isDebugEnabled()) {
		  logger.debug("Closing Connection[{}] slot[{}] reason[{}] stats: {} , pstmtStats: {} ", name, slotId, closeReason, stats.getValues(false), pstmtCache.getDescription());
	  }

		try {
			if (connection.isClosed()) {
			  // Typically the JDBC Driver has its own JVM shutdown hook and already 
			  // closed the connections in our DataSource pool so making this DEBUG level
				logger.debug("Closing Connection[{}] that is already closed?", name);
				return;
			}
		} catch (SQLException ex) {
			if (logErrors) {
				logger.error("Error checking if connection [" + getNameSlot() + "] is closed", ex);
			}
		}

		try {
			for (ExtendedPreparedStatement ps : pstmtCache.values()) {
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
			if (logErrors || logger.isDebugEnabled()) {
				logger.error("Error when fully closing connection [" + getNameSlot() + "]", ex);
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

	public Statement createStatement(int resultSetType, int resultSetConcurreny) throws SQLException {
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
		  if (!pstmtCache.returnStatement(pstmt)) {
		    try {
          // Already an entry in the cache with the exact same SQL...
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
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "prepareStatement()");
		}
		try {
			synchronized (pstmtMonitor) {
				lastStatement = sql;
	
				// try to get a matching cached PStmt from the cache.
				ExtendedPreparedStatement pstmt = pstmtCache.remove(cacheKey);
	
				if (pstmt != null) {
					return pstmt;
				}
	
				// create a new PreparedStatement
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

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurreny) throws SQLException {
	  
		if (status == STATUS_IDLE) {
			throw new SQLException(IDLE_CONNECTION_ACCESSED_ERROR + "prepareStatement()");
		}
		try {
			// no caching when creating PreparedStatements this way
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
		this.exeStartNanos = System.nanoTime();
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

		long durationNanos = System.nanoTime() - exeStartNanos;
		stats.add(durationNanos, hadErrors);
		
		if (hadErrors) {
			if (!pool.validateConnection(this)) {
				// the connection is BAD, remove it, close it and test the pool
				pool.returnConnectionForceClose(this);
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
      // the connection is BAD, remove it, close it and test the pool
		  logger.warn("Error when trying to return connection to pool, closing fully.", ex);
      pool.returnConnectionForceClose(this);
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
				logger.warn("Closing Connection on finalize() - {}", getFullDescription());
				closeConnectionFully(false);
			}
		} catch (Exception e) {
			logger.error("Error when finalize is closing a connection? (unexpected)", e);
		}
		super.finalize();
	}

	/**
	 * Return true if the connection is too old.
	 */
  public boolean exceedsMaxAge(long maxAgeMillis) {
    if (maxAgeMillis > 0 && (creationTime < (System.currentTimeMillis() - maxAgeMillis))){
      this.closeReason = REASON_MAXAGE;
      return true;
    }
    return false;
  }
  
  public boolean shouldTrimOnReturn(long lastResetTime, long maxAgeMillis) {
    if (creationTime <= lastResetTime) {
      this.closeReason = REASON_RESET;
      return true;
    }
    if (exceedsMaxAge(maxAgeMillis)) {
      return true;
    }
    return false;
  }
  
  /**
   * Return true if the connection has been idle for too long or is too old.
   */
	public boolean shouldTrim(long usedSince, long createdSince) {
	  if (lastUseTime < usedSince) {
	    // been idle for too long so trim it
	    this.closeReason = REASON_IDLE;
	    return true;
	  }
	  if (createdSince > 0 && createdSince > creationTime) {
	    // exceeds max age so trim it
	    this.closeReason = REASON_MAXAGE;
	    return true;
	  }
	  return false;
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

  public Map<String, Class<?>> getTypeMap() throws SQLException {
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

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurreny) throws SQLException {
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

  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
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
   * Return the stackTrace as a String for logging purposes.
   */
  public String getStackTraceAsString() {
    StackTraceElement[] stackTrace = getStackTrace();
    if (stackTrace == null){
      return "";
    }
    return Arrays.toString(stackTrace);
  }

  /**
   * Return the full stack trace that got the connection from the pool. You
   * could use this if getCreatedByMethod() doesn't work for you.
   */
  public StackTraceElement[] getStackTrace() {

    if (stackTrace == null) {
      return null;
    }

    // filter off the top of the stack that we are not interested in
    ArrayList<StackTraceElement> filteredList = new ArrayList<StackTraceElement>();
    boolean include = false;
    for (int i = 0; i < stackTrace.length; i++) {
      if (!include && !skipElement(stackTrace[i].toString())) {
        include = true;
      }
      if (include && filteredList.size() < maxStackTrace) {
        filteredList.add(stackTrace[i]);
      }
    }
    return filteredList.toArray(new StackTraceElement[filteredList.size()]);

  }

}
