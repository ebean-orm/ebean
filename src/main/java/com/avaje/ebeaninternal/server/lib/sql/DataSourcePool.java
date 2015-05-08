package com.avaje.ebeaninternal.server.lib.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebeaninternal.api.ClassUtil;

/**
 * A robust DataSource.
 * <p>
 * <ul>
 * <li>Manages the number of connections closing connections that have been idle
 * for some time.
 * <li>Notifies when the datasource goes down and comes back up.
 * <li>Checks for expected downtime which is useful for schedule db backups.
 * <li>Provides PreparedStatement caching
 * <li>Knows the busy connections
 * <li>Traces connections that have been leaked
 * </ul>
 * </p>
 */
public class DataSourcePool implements DataSource {

    private static final Logger logger = LoggerFactory.getLogger(DataSourcePool.class);

    /**
     * The name given to this dataSource.
     */
    private final String name;

    /**
     * Used to notify of changes to the DataSource status.
     */
    private final DataSourceAlert notify;

    /**
     * Optional listener that can be notified when connections are got from and
     * put back into the pool.
     */
    private final DataSourcePoolListener poolListener;

    /**
     * Properties used to create a Connection.
     */
    private final Properties connectionProps;

    /**
     * The jdbc connection url.
     */
    private final String databaseUrl;

    /**
     * The jdbc driver.
     */
    private final String databaseDriver;

    /**
     * The sql used to test a connection.
     */
    private final String heartbeatsql;
        
    private final int heartbeatFreqSecs;
    
    private final int heartbeatTimeoutSeconds;
    

    private final long trimPoolFreqMillis;

    /**
     * The transaction isolation level as per java.sql.Connection.
     */
    private final int transactionIsolation;

    /**
     * The default autoCommit setting for Connections in this pool.
     */
    private final boolean autoCommit;

    /**
     * Max idle time in millis.
     */
    private final int maxInactiveMillis;
    
    /**
     * Max age a connection is allowed in millis. 
     * A value of 0 means no limit (no trimming based on max age).
     */
    private final long maxAgeMillis;

    /**
     * Flag set to true to capture stackTraces (can be expensive).
     */
    private boolean captureStackTrace;

    /**
     * The max size of the stack trace to report.
     */
    private int maxStackTraceSize;

    /**
     * flag to indicate we have sent an alert message.
     */
    private boolean dataSourceDownAlertSent;

    /**
     * The time the pool was last trimmed.
     */
    private long lastTrimTime;

    /**
     * Assume that the DataSource is up. heartBeat checking will discover when
     * it goes down, and comes back up again.
     */
    private boolean dataSourceUp = true;

    /**
     * The current alert.
     */
    private boolean inWarningMode;

    /**
     * The minimum number of connections this pool will maintain.
     */
    private int minConnections;

    /**
     * The maximum number of connections this pool will grow to.
     */
    private int maxConnections;

    /**
     * The number of connections to exceed before a warning Alert is fired.
     */
    private int warningSize;

    /**
     * The time a thread will wait for a connection to become available.
     */
    private final int waitTimeoutMillis;

    /**
     * The size of the preparedStatement cache;
     */
    private int pstmtCacheSize;

    private final PooledConnectionQueue queue;

    /**
     * Used to find and close() leaked connections. Leaked connections are
     * thought to be busy but have not been used for some time. Each time a
     * connection is used it sets it's lastUsedTime.
     */
    private long leakTimeMinutes;

    private final Runnable heartbeatRunnable = new HeartBeatRunnable();
    
    public DataSourcePool(DataSourceAlert notify, String name, DataSourceConfig params) {

        this.notify = notify;
        this.name = name;
        this.poolListener = createPoolListener(params.getPoolListener());

        this.autoCommit = params.isAutoCommit();
        this.transactionIsolation = params.getIsolationLevel();
        
        this.maxInactiveMillis = 1000 * params.getMaxInactiveTimeSecs();
        this.maxAgeMillis = 60000 * params.getMaxAgeMinutes();
        this.leakTimeMinutes = params.getLeakTimeMinutes();
        this.captureStackTrace = params.isCaptureStackTrace();
        this.maxStackTraceSize = params.getMaxStackTraceSize();
        this.databaseDriver = params.getDriver();
        this.databaseUrl = params.getUrl();
        this.pstmtCacheSize = params.getPstmtCacheSize();

        this.minConnections = params.getMinConnections();
        this.maxConnections = params.getMaxConnections();
        this.waitTimeoutMillis = params.getWaitTimeoutMillis();
        this.heartbeatsql = params.getHeartbeatSql();
        this.heartbeatFreqSecs = params.getHeartbeatFreqSecs();
        this.heartbeatTimeoutSeconds = params.getHeartbeatTimeoutSeconds();
        this.trimPoolFreqMillis = 1000 * params.getTrimPoolFreqSecs();
        
        queue = new PooledConnectionQueue(this);

        String un = params.getUsername();
        String pw = params.getPassword();
        if (un == null) {
            throw new RuntimeException("DataSource user is null?");
        }
        if (pw == null) {
            throw new RuntimeException("DataSource password is null?");
        }
        this.connectionProps = new Properties();
        this.connectionProps.setProperty("user", un);
        this.connectionProps.setProperty("password", pw);
        
        Map<String, String> customProperties = params.getCustomProperties();
        if (customProperties != null){
          Set<Entry<String,String>> entrySet = customProperties.entrySet();
          for (Entry<String, String> entry : entrySet) {
            this.connectionProps.setProperty(entry.getKey(), entry.getValue());
          }
        }

        try {
            initialise();
        } catch (SQLException ex) {
            throw new DataSourceException(ex);
        }
    }
    
    class HeartBeatRunnable implements Runnable {
      @Override
      public void run() {
        checkDataSource();
      }
    }

    
    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
      throw new SQLFeatureNotSupportedException("We do not support java.util.logging");
    }

    /**
     * Create the DataSourcePoolListener if there is one.
     */
    private DataSourcePoolListener createPoolListener(String cn) {
        if (cn == null) {
            return null;
        }
        try {
            return (DataSourcePoolListener)ClassUtil.newInstance(cn, this.getClass());
        } catch (Exception e) {
            throw new DataSourceException(e);
        }
    }

    private void initialise() throws SQLException {

        // Ensure database driver is loaded
        try {
            ClassUtil.forName(this.databaseDriver, this.getClass());
        } catch (Throwable e) {
            throw new PersistenceException("Problem loading Database Driver [" + this.databaseDriver + "]: "
                    + e.getMessage(), e);
        }

        String transIsolation = TransactionIsolation.getLevelDescription(transactionIsolation);
        StringBuilder sb = new StringBuilder();
        sb.append("DataSourcePool [").append(name);
        sb.append("] autoCommit[").append(autoCommit);
        sb.append("] transIsolation[").append(transIsolation);
        sb.append("] min[").append(minConnections);
        sb.append("] max[").append(maxConnections).append("]");

        logger.info(sb.toString());

        queue.ensureMinimumConnections();
    }

    /**
     * Returns false.
     */
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        return false;
    }

    /**
     * Not Implemented.
     */
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        throw new SQLException("Not Implemented");
    }

    /**
     * Return the dataSource name.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the max size of stack traces used when trying to find connection pool leaks.
     * <p>
     * This is only used when {@link #isCaptureStackTrace()} is true. 
     * </p>
     */
    public int getMaxStackTraceSize() {
        return maxStackTraceSize;
    }

    /**
     * Returns false when the dataSource is down.
     */
    public boolean isDataSourceUp() {
        return dataSourceUp;
    }

    /**
     * Called when the pool hits the warning level.
     */
    protected void notifyWarning(String msg) {

        if (!inWarningMode) {
            // send an Error to the event log...
            inWarningMode = true;
            logger.warn(msg);
            if (notify != null) {
                String subject = "DataSourcePool [" + name + "] warning";
                notify.dataSourceWarning(subject, msg);
            }
        }
    }

    private void notifyDataSourceIsDown(SQLException ex) {

        if (!dataSourceDownAlertSent) {
            logger.error("FATAL: DataSourcePool [" + name + "] is down or has network error!!!", ex);
            if (notify != null) {
                notify.dataSourceDown(name);
            }
            dataSourceDownAlertSent = true;
        }
        if (dataSourceUp) {
            reset();
        }
        dataSourceUp = false;
    }

    private void notifyDataSourceIsUp() {
        if (dataSourceDownAlertSent) {
            logger.error("RESOLVED FATAL: DataSourcePool [" + name + "] is back up!");
            if (notify != null) {
                notify.dataSourceUp(name);
            }
            dataSourceDownAlertSent = false;

        } else if (!dataSourceUp) {
            logger.info("DataSourcePool [" + name + "] is back up!");
        }

        if (!dataSourceUp) {
            dataSourceUp = true;
            reset();
        }
    }
    
    
    /**
     * Return the heartbeat frequency in seconds.
     * <p>
     * This is the frequency that the heartbeat runnable should be run.
     * </p>
     */
    public int getHeartbeatFreqSecs() {
      return heartbeatFreqSecs;
    }

    /**
     * Returns the Runnable used to check the dataSource using a heartbeat query.
     */
    public Runnable getHeartbeatRunnable() {
      return heartbeatRunnable;
    }

    /**
     * Trim connections (in the free list) based on idle time and maximum age.
     */
    private void trimIdleConnections() {
      if (System.currentTimeMillis() > (lastTrimTime + trimPoolFreqMillis)) {
        try {
          queue.trim(maxInactiveMillis, maxAgeMillis);
          lastTrimTime = System.currentTimeMillis();
        } catch (Exception e) {
          logger.error("Error trying to trim idle connections", e);
        }
      }
    }
    
    /**
     * Check the dataSource is up. Trim connections.
     * <p>
     * This is called by the HeartbeatRunnable which should be scheduled to 
     * run periodically (every heartbeatFreqSecs seconds actually).
     * </p>
     */
    public void checkDataSource() {
      
        // first trim idle connections
        trimIdleConnections();
      
        Connection conn = null;
        try {
            // Get a connection from the pool and test it
            conn = getConnection();
            if (testConnection(conn)) {
              notifyDataSourceIsUp();
              
            } else {
              notifyDataSourceIsDown(null);              
            }

        } catch (SQLException ex) {
            notifyDataSourceIsDown(ex);
            
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                logger.warn("Can't close connection in checkDataSource!");
            }
        }
    }

    /**
     * Create a Connection that will not be part of the connection pool.
     * 
     * <p>
     * When this connection is closed it will not go back into the pool.
     * </p>
     * 
     * <p>
     * If withDefaults is true then the Connection will have the autoCommit and
     * transaction isolation set to the defaults for the pool.
     * </p>
     */
    public Connection createUnpooledConnection() throws SQLException {

        try {
            Connection conn = DriverManager.getConnection(databaseUrl, connectionProps);
            conn.setAutoCommit(autoCommit);
            conn.setTransactionIsolation(transactionIsolation);
            return conn;

        } catch (SQLException ex) {
            notifyDataSourceIsDown(null);
            throw ex;
        }
    }

    /**
     * Set a new maximum size. The pool should respect this new maximum
     * immediately and not require a restart. You may want to increase the
     * maxConnections if the pool gets large and hits the warning level.
     */
    public void setMaxSize(int max) {
        queue.setMaxSize(max);
        this.maxConnections = max;
    }

    /**
     * Return the max size this pool can grow to.
     */
    public int getMaxSize() {
        return maxConnections;
    }

    /**
     * Set the min size this pool should maintain.
     */
    public void setMinSize(int min) {
        queue.setMinSize(min);
        this.minConnections = min;
    }

    /**
     * Return the min size this pool should maintain.
     */
    public int getMinSize() {
        return minConnections;
    }

    /**
     * Set a new maximum size. The pool should respect this new maximum
     * immediately and not require a restart. You may want to increase the
     * maxConnections if the pool gets large and hits the warning and or alert
     * levels.
     */
    public void setWarningSize(int warningSize) {
        queue.setWarningSize(warningSize);
        this.warningSize = warningSize;
    }

    /**
     * Return the warning size. When the pool hits this size it can send a 
     * notify message to an administrator.
     */
    public int getWarningSize() {
        return warningSize;
    }

    /**
     * Return the time in millis that threads will wait when the pool has hit
     * the max size. These threads wait for connections to be returned by the
     * busy connections.
     */
    public int getWaitTimeoutMillis() {
        return waitTimeoutMillis;
    }

    /**
     * Return the time after which inactive connections are trimmed.
     */
    public int getMaxInactiveMillis() {
        return maxInactiveMillis;
    }
    
  /**
   * Return the maximum age a connection is allowed to be before it is trimmed
   * out of the pool. This value can be 0 which means there is no maximum age.
   */
    public long getMaxAgeMillis() {
      return maxAgeMillis;
    }

    private boolean testConnection(Connection conn) throws SQLException {

        if (heartbeatsql == null) {
            return conn.isValid(heartbeatTimeoutSeconds);
        }
        Statement stmt = null;
        ResultSet rset = null;
        try {
            // It should only error IF the DataSource is down or a network issue
            stmt = conn.createStatement();
            if (heartbeatTimeoutSeconds > 0) {
              stmt.setQueryTimeout(heartbeatTimeoutSeconds);
            }
            rset = stmt.executeQuery(heartbeatsql);
            conn.commit();
            
            return true;

        } finally {
            try {
                if (rset != null) {
                    rset.close();
                }
            } catch (SQLException e) {
                logger.error(null, e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                logger.error(null, e);
            }
        }
    }

    /**
     * Make sure the connection is still ok to use. If not then remove it from
     * the pool.
     */
    protected boolean validateConnection(PooledConnection conn) {
        try {
            return testConnection(conn);

        } catch (Exception e) {
            logger.warn("heartbeatsql test failed on connection[" + conn.getName() + "]");
            return false;
        }
    }

    /**
     * Called by the PooledConnection themselves, returning themselves to the
     * pool when they have been finished with.
     * <p>
     * Note that connections may not be added back to the pool if returnToPool
     * is false or if they where created before the recycleTime. In both of
     * these cases the connection is fully closed and not pooled.
     * </p>
     * 
     * @param pooledConnection
     *            the returning connection
     * 
     */
    protected void returnConnection(PooledConnection pooledConnection) {

      // return a normal 'good' connection
      returnTheConnection(pooledConnection, false);
    }

    /**
     * This is a bad connection and must be removed from the pool's busy list and fully closed.
     */
    protected void returnConnectionForceClose(PooledConnection pooledConnection) {

      returnTheConnection(pooledConnection, true);
    }

    /**
     * Return connection. If forceClose is true then this is a bad connection that
     * must be removed and closed fully.
     */
    private void returnTheConnection(PooledConnection pooledConnection, boolean forceClose) {

      if (poolListener != null && !forceClose) {
          poolListener.onBeforeReturnConnection(pooledConnection);
      }
      queue.returnPooledConnection(pooledConnection, forceClose);
      
      if (forceClose) {
        // Got a bad connection so check the pool
        checkDataSource();
      }
    }

    /**
     * Collect statistics of a connection that is fully closing
     */
    protected void reportClosingConnection(PooledConnection pooledConnection) {
      
      queue.reportClosingConnection(pooledConnection);
    }
    
    /**
     * Returns information describing connections that are currently being used.
     */
    public String getBusyConnectionInformation() {

        return queue.getBusyConnectionInformation();
    }

    /**
     * Dumps the busy connection information to the logs.
     * <p>
     * This includes the stackTrace elements if they are being captured. This is
     * useful when needing to look a potential connection pool leaks.
     * </p>
     */
    public void dumpBusyConnectionInformation() {

        queue.dumpBusyConnectionInformation();
    }

    /**
     * Close any busy connections that have not been used for some time.
     * <p>
     * These connections are considered to have leaked from the connection pool.
     * </p>
     * <p>
     * Connection leaks occur when code doesn't ensure that connections are
     * closed() after they have been finished with. There should be an
     * appropriate try catch finally block to ensure connections are always
     * closed and put back into the pool.
     * </p>
     */
    public void closeBusyConnections(long leakTimeMinutes) {

        queue.closeBusyConnections(leakTimeMinutes);
    }

    /**
     * Grow the pool by creating a new connection. The connection can either be
     * added to the available list, or returned.
     * <p>
     * This method is protected by synchronization in calling methods.
     * </p>
     */
    protected PooledConnection createConnectionForQueue(int connId) throws SQLException {

        try {
            Connection c = createUnpooledConnection();

            PooledConnection pc = new PooledConnection(this, connId, c);
            pc.resetForUse();

            if (!dataSourceUp) {
                notifyDataSourceIsUp();
            }
            return pc;

        } catch (SQLException ex) {
            notifyDataSourceIsDown(ex);
            throw ex;
        }
    }

    /**
     * Close all the connections in the pool.
     * <p>
     * <ul>
     * <li>Checks that the database is up.
     * <li>Resets the Alert level.
     * <li>Closes busy connections that have not been used for some time (aka
     * leaks).
     * <li>This closes all the currently available connections.
     * <li>Busy connections are closed when they are returned to the pool.
     * </ul>
     * </p>
     */
    public void reset() {
        queue.reset(leakTimeMinutes);
        inWarningMode = false;
    }

    /**
     * Return a pooled connection.
     */
    public Connection getConnection() throws SQLException {
        return getPooledConnection();
    }

    /**
     * Get a connection from the pool.
     * <p>
     * This will grow the pool if all the current connections are busy. This
     * will go into a wait if the pool has hit its maximum size.
     * </p>
     */
    public PooledConnection getPooledConnection() throws SQLException {

        PooledConnection c = queue.getPooledConnection();

        if (captureStackTrace) {
            c.setStackTrace(Thread.currentThread().getStackTrace());
        }

        if (poolListener != null) {
            poolListener.onAfterBorrowConnection(c);
        }
        return c;
    }

    /**
     * Send a message to the DataSourceAlertListener to test it. This is so that
     * you can make sure the alerter is configured correctly etc.
     */
    public void testAlert() {

        String subject = "Test DataSourcePool [" + name + "]";
        String msg = "Just testing if alert message is sent successfully.";

        if (notify != null) {
            notify.dataSourceWarning(subject, msg);
        }
    }

    /**
     * This will close all the free connections, and then go into a wait loop,
     * waiting for the busy connections to be freed.
     * 
     * <p>
     * The DataSources's should be shutdown AFTER thread pools. Leaked
     * Connections are not waited on, as that would hang the server.
     * </p>
     */
    public void shutdown(boolean deregisterDriver) {
        queue.shutdown();
        if (deregisterDriver){
          deregisterDriver();
        }
    }

    /**
     * Return the default autoCommit setting Connections in this pool will use.
     * 
     * @return true if the pool defaults autoCommit to true
     */
    public boolean getAutoCommit() {
        return autoCommit;
    }

    /**
     * Return the default transaction isolation level connections in this pool
     * should have.
     * 
     * @return the default transaction isolation level
     */
    public int getTransactionIsolation() {
        return transactionIsolation;
    }

    /**
     * Return true if the connection pool is currently capturing the StackTrace
     * when connections are 'got' from the pool.
     * <p>
     * This is set to true to help diagnose connection pool leaks.
     * </p>
     */
    public boolean isCaptureStackTrace() {
        return captureStackTrace;
    }

    /**
     * Set this to true means that the StackElements are captured every time a
     * connection is retrieved from the pool. This can be used to identify
     * connection pool leaks.
     */
    public void setCaptureStackTrace(boolean captureStackTrace) {
        this.captureStackTrace = captureStackTrace;
    }

    /**
     * Not implemented and shouldn't be used.
     */
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * Not implemented and shouldn't be used.
     */
    public int getLoginTimeout() throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * Not implemented and shouldn't be used.
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * Returns null.
     */
    public PrintWriter getLogWriter() {
        return null;
    }

    /**
     * Not implemented.
     */
    public void setLogWriter(PrintWriter writer) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * For detecting and closing leaked connections. Connections that have been
     * busy for more than leakTimeMinutes are considered leaks and will be
     * closed on a reset().
     * <p>
     * If you want to use a connection for that longer then you should consider
     * creating an unpooled connection or setting longRunning to true on that
     * connection.
     * </p>
     */
    public void setLeakTimeMinutes(long leakTimeMinutes) {
        this.leakTimeMinutes = leakTimeMinutes;
    }

    /**
     * Return the number of minutes after which a busy connection could be
     * considered leaked from the connection pool.
     */
    public long getLeakTimeMinutes() {
        return leakTimeMinutes;
    }

    /**
     * Return the preparedStatement cache size.
     */
    public int getPstmtCacheSize() {
        return pstmtCacheSize;
    }

    /**
     * Set the preparedStatement cache size.
     */
    public void setPstmtCacheSize(int pstmtCacheSize) {
        this.pstmtCacheSize = pstmtCacheSize;
    }

    /**
     * Return the current status of the connection pool.
     * <p>
     * If you pass reset = true then the counters such as 
     * hitCount, waitCount and highWaterMark are reset.
     * </p>
     */
    public Status getStatus(boolean reset) {
        return queue.getStatus(reset);
    }
    
    /**
     * Return the aggregated load statistics collected on all the connections in the pool.
     */
    public DataSourcePoolStatistics getStatistics(boolean reset) {
      
      return queue.getStatistics(reset);
    }

    /**
     * Deregister the JDBC driver.
     */
	public void deregisterDriver() {
	    try {
	      logger.debug("Deregistered the JDBC driver "+this.databaseDriver);
	    	DriverManager.deregisterDriver(DriverManager.getDriver(this.databaseUrl));
	    } catch (SQLException e) {
	    	logger.warn("Error trying to deregister the JDBC driver "+this.databaseDriver, e);
	    }
    }    

    public static class Status {

        private final String name;
        private final int minSize;
        private final int maxSize;
        private final int free;
        private final int busy;
        private final int waiting;
        private final int highWaterMark;
        private final int waitCount;
        private final int hitCount;

        protected Status(String name, int minSize, int maxSize, int free, int busy, int waiting, int highWaterMark,
                int waitCount, int hitCount) {
            this.name = name;
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.free = free;
            this.busy = busy;
            this.waiting = waiting;
            this.highWaterMark = highWaterMark;
            this.waitCount = waitCount;
            this.hitCount = hitCount;
        }

        public String toString() {
            return "min[" + minSize + "] max[" + maxSize + "] free[" + free + "] busy[" + busy + "] waiting[" + waiting
                    + "] highWaterMark[" + highWaterMark + "] waitCount[" + waitCount + "] hitCount[" + hitCount+"]";
        }

        /**
         * Return the DataSource name.
         */
        public String getName() {
            return name;
        }

        /**
         * Return the min pool size.
         */
        public int getMinSize() {
            return minSize;
        }

        /**
         * Return the max pool size.
         */
        public int getMaxSize() {
            return maxSize;
        }

        /**
         * Return the current number of free connections in the pool.
         */
        public int getFree() {
            return free;
        }

        /**
         * Return the current number of busy connections in the pool.
         */
        public int getBusy() {
            return busy;
        }

        /**
         * Return the current number of threads waiting for a connection.
         */
        public int getWaiting() {
            return waiting;
        }

        /**
         * Return the high water mark of busy connections.
         */
        public int getHighWaterMark() {
            return highWaterMark;
        }

        /**
         * Return the total number of times a thread had to wait.
         */
        public int getWaitCount() {
            return waitCount;
        }

        /**
         * Return the total number of times there was an attempt to get a
         * connection.
         * <p>
         * If the attempt to get a connection failed with a timeout or other
         * exception those attempts are still included in this hit count.
         * </p>
         */
        public int getHitCount() {
            return hitCount;
        }

    }

}
