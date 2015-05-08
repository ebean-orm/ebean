package com.avaje.ebeaninternal.server.lib.sql;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebeaninternal.server.lib.sql.DataSourcePool.Status;
import com.avaje.ebeaninternal.server.lib.sql.PooledConnectionStatistics.LoadValues;

public class PooledConnectionQueue {

    private static final Logger logger = LoggerFactory.getLogger(PooledConnectionQueue.class);
    
    private static final TimeUnit MILLIS_TIME_UNIT = TimeUnit.MILLISECONDS;

    private final String name;
    
    private final DataSourcePool pool;
        
    /**
     * A 'circular' buffer designed specifically for free connections.
     */
    private final FreeConnectionBuffer freeList;
    
    /**
     * A 'slots' buffer designed specifically for busy connections.
     * Fast add remove based on slot id.
     */
    private final BusyConnectionBuffer busyList;

    /**
     * Load statistics collected off connections that have closed fully (left the pool).
     */
    private final PooledConnectionStatistics collectedStats = new PooledConnectionStatistics();
    
    /**
     * Currently accumulated load statistics.
     */
    private LoadValues accumulatedValues = new LoadValues();

    /** 
     * Main lock guarding all access 
     */
    private final ReentrantLock lock;
    
    /** 
     * Condition for threads waiting to take a connection 
     */
    private final Condition notEmpty;

    private int connectionId;

    private final long waitTimeoutMillis;
    
    private final long leakTimeMinutes;
    
    private final long maxAgeMillis;

    private int warningSize;
    
    private int maxSize;
    
    private int minSize;
    
    /**
     * Number of threads in the wait queue.
     */
    private int waitingThreads;
    
    /**
     * Number of times a thread had to wait.
     */
    private int waitCount;
    
    /**
     * Number of times a connection was got from this queue.
     */
    private int hitCount;
    
    /**
     * The high water mark for the queue size.
     */
    private int highWaterMark;
    
    /**
     * Last time the pool was reset. Used to close busy connections as they are
     * returned to the pool that where created prior to the lastResetTime.
     */
    private long lastResetTime;

    private boolean doingShutdown;

    public PooledConnectionQueue(DataSourcePool pool) {
        
        this.pool = pool;
        this.name = pool.getName();
        this.minSize = pool.getMinSize();
        this.maxSize = pool.getMaxSize();
        
        this.warningSize = pool.getWarningSize();
        this.waitTimeoutMillis = pool.getWaitTimeoutMillis();
        this.leakTimeMinutes = pool.getLeakTimeMinutes();
        this.maxAgeMillis = pool.getMaxAgeMillis();

        this.busyList = new BusyConnectionBuffer(maxSize, 20);
        this.freeList = new FreeConnectionBuffer();

        this.lock = new ReentrantLock(false);
        this.notEmpty = lock.newCondition();
    }
    
    private Status createStatus() {
        return new Status(name, minSize, maxSize, freeList.size(), busyList.size(), waitingThreads, highWaterMark, waitCount, hitCount);        
    }
    
    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return createStatus().toString();
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Collect statistics of a connection that is fully closing
     */
    protected void reportClosingConnection(PooledConnection pooledConnection) {
      
      collectedStats.add(pooledConnection.getStatistics());
    }

    public DataSourcePoolStatistics getStatistics(boolean reset) {
      
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {

        LoadValues aggregate = collectedStats.getValues(reset);

        freeList.collectStatistics(aggregate, reset);
        busyList.collectStatistics(aggregate, reset);

        aggregate.plus(accumulatedValues);
        
        this.accumulatedValues = (reset) ? new LoadValues() : aggregate;
        
        return new DataSourcePoolStatistics(aggregate.getCollectionStart(), aggregate.getCount(), aggregate.getErrorCount(), aggregate.getHwmMicros(), aggregate.getTotalMicros());
        
      } finally {
          lock.unlock();
      }
  }
    
    public Status getStatus(boolean reset) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Status s = createStatus();
            if (reset){
                highWaterMark = busyList.size();
                hitCount = 0;
                waitCount = 0;
            }
            return s;
        } finally {
            lock.unlock();
        }
    }
    
    public void setMinSize(int minSize) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (minSize > this.maxSize){
                throw new IllegalArgumentException("minSize "+minSize+" > maxSize "+this.maxSize);
            }
            this.minSize = minSize;
        } finally {
            lock.unlock();
        }
    }
    
    public void setMaxSize(int maxSize) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (maxSize < this.minSize){
                throw new IllegalArgumentException("maxSize "+maxSize+" < minSize "+this.minSize);
            }
            this.busyList.setCapacity(maxSize);
            this.maxSize = maxSize;
        } finally {
            lock.unlock();
        }
    }
    
    public void setWarningSize(int warningSize) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (warningSize > this.maxSize){
                throw new IllegalArgumentException("warningSize "+warningSize+" > maxSize "+this.maxSize);
            }
            this.warningSize = warningSize;
        } finally {
            lock.unlock();
        }
    }
    
    private int totalConnections() {
        return freeList.size() + busyList.size();
    }
    
    public void ensureMinimumConnections() throws SQLException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int add = minSize - totalConnections();
            if (add > 0){
                for (int i = 0; i < add; i++) {
                    PooledConnection c = pool.createConnectionForQueue(connectionId++);
                    freeList.add(c);
                }
                notEmpty.signal();
            }
           
        } finally {
            lock.unlock();
        }        
    }
    
    /**
     * Return a PooledConnection.
     */
    protected void returnPooledConnection(PooledConnection c, boolean forceClose) {
        
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {            
            if (!busyList.remove(c)) {
                logger.error("Connection [{}] not found in BusyList? ", c);
            }
            if (forceClose || c.shouldTrimOnReturn(lastResetTime, maxAgeMillis)) {
                c.closeConnectionFully(false);
                
            } else {
                freeList.add(c);
                notEmpty.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    private PooledConnection extractFromFreeList() {
        PooledConnection c = freeList.remove();
        registerBusyConnection(c);
        return c;
    }

    public PooledConnection getPooledConnection() throws SQLException {
        
        try {
            PooledConnection pc = _getPooledConnection();
            pc.resetForUse();
            return pc;
            
        } catch (InterruptedException e) {
            String msg = "Interrupted getting connection from pool "+e;
            throw new SQLException(msg);
        }
    }
     
    /**
     * Register the PooledConnection with the busyList.
     */
    private int registerBusyConnection(PooledConnection c) {
        int busySize = busyList.add(c);
        if (busySize > highWaterMark){
            highWaterMark = busySize;
        }
        return busySize;
    }
    
    private PooledConnection _getPooledConnection() throws InterruptedException, SQLException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            if (doingShutdown) {
                throw new SQLException("Trying to access the Connection Pool when it is shutting down");
            }
            
            // this includes attempts that fail with InterruptedException 
            // or SQLException but that is ok as its only an indicator 
            hitCount++;
            
            // are other threads already waiting? (they get priority)
            if (waitingThreads == 0){
                
                if (!freeList.isEmpty()){
                    // we have a free connection to return
                    return extractFromFreeList();
                } 
                
                if (busyList.size() < maxSize){
                    // grow the connection pool
                    PooledConnection c = pool.createConnectionForQueue(connectionId++);
                    int busySize = registerBusyConnection(c);
                    
                    if (logger.isDebugEnabled()) {
                      logger.debug("DataSourcePool [{}] grow; id[{}] busy[{}] max[{}]", name, c.getName(), busySize, maxSize);
                    }
                    checkForWarningSize();
                    return c;
                }
            }
            
            try {
                // The pool is at maximum size. We are going to go into
                // a wait loop until connections are returned into the pool.
                waitCount++;
                waitingThreads++;
                return _getPooledConnectionWaitLoop();
            } finally {
                waitingThreads--;
            }

        } finally {
            lock.unlock();
        } 
    }
    
    /**
     * Got into a loop waiting for connections to be returned to the pool.
     */
    private PooledConnection _getPooledConnectionWaitLoop() throws SQLException, InterruptedException {

        long nanos = MILLIS_TIME_UNIT.toNanos(waitTimeoutMillis);
        for (;;) {
            
            if (nanos <= 0) {
                String msg = "Unsuccessfully waited ["+waitTimeoutMillis+"] millis for a connection to be returned."
                    + " No connections are free. You need to Increase the max connections of ["+maxSize+"]"
                    + " or look for a connection pool leak using datasource.xxx.capturestacktrace=true";
                if (pool.isCaptureStackTrace()) {
                    dumpBusyConnectionInformation();
                }

                throw new SQLException(msg);
            }
            
            try {
                nanos = notEmpty.awaitNanos(nanos);
                if (!freeList.isEmpty()) {
                    // successfully waited 
                    return extractFromFreeList();
                }
            } catch (InterruptedException ie) {
                notEmpty.signal(); // propagate to non-interrupted thread
                throw ie;
            }
        }
    }
    
    public void shutdown() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            doingShutdown = true;
            Status status = createStatus();
            DataSourcePoolStatistics statistics = pool.getStatistics(false);
            logger.debug("DataSourcePool [{}] shutdown {} - Statistics {}", name, status, statistics);
            
            closeFreeConnections(true);
        
            if (!busyList.isEmpty()) {
                logger.warn("Closing busy connections on shutdown size: "+ busyList.size());
                dumpBusyConnectionInformation();
                closeBusyConnections(0);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Close all the connections in the pool and any current busy connections
     * when they are returned. New connections will be then created on demand.
     * <p>
     * This is typically done when a database down event occurs.
     * </p>
     */
    public void reset(long leakTimeMinutes) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Status status = createStatus();
            logger.info("Reseting DataSourcePool [{}] {}", name, status);
            lastResetTime = System.currentTimeMillis();

            closeFreeConnections(false);
            closeBusyConnections(leakTimeMinutes);

            logger.info("Busy Connections:\n" + getBusyConnectionInformation());

        } finally {
            lock.unlock();
        }
    }

    public void trim(long maxInactiveMillis, long maxAgeMillis) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (trimInactiveConnections(maxInactiveMillis, maxAgeMillis) > 0) {
              try {
                ensureMinimumConnections();
              } catch (SQLException e) {
                logger.error("Error trying to ensure minimum connections", e);
              }
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Trim connections that have been not used for some time.
     */
    private int trimInactiveConnections(long maxInactiveMillis, long maxAgeMillis) {
            
        long usedSince = System.currentTimeMillis() - maxInactiveMillis;
        long createdSince = (maxAgeMillis == 0) ? 0 : System.currentTimeMillis() - maxAgeMillis;
        
        int trimedCount = freeList.trim(usedSince, createdSince);
        if (trimedCount > 0) {
            logger.debug("DataSourcePool [{}] trimmed [{}] inactive connections. New size[{}]", name, trimedCount, totalConnections());
        }
        return trimedCount;
    }
    
    /**
     * Close all the connections that are in the free list.
     */
    public void closeFreeConnections(boolean logErrors) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
          freeList.closeAll(logErrors);
        } finally {
            lock.unlock();
        }
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

        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            busyList.closeBusyConnections(leakTimeMinutes);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * As the pool grows it gets closer to the maxConnections limit. We can send
     * an Alert (or warning) as we get close to this limit and hence an
     * Administrator could increase the pool size if desired.
     * <p>
     * This is called whenever the pool grows in size (towards the max limit).
     * </p>
     */
    private void checkForWarningSize() {

        // the the total number of connections that we can add 
        // to the pool before it hits the maximum
        int availableGrowth = (maxSize - totalConnections());

        if (availableGrowth < warningSize) {

            closeBusyConnections(leakTimeMinutes);

            String msg = "DataSourcePool [" + name + "] is [" + availableGrowth+ "] connections from its maximum size.";
            pool.notifyWarning(msg);  
        } 
    }
    
    public String getBusyConnectionInformation() {
        return getBusyConnectionInformation(false);
    }
    
    public void dumpBusyConnectionInformation() {
        getBusyConnectionInformation(true);
    }
    
    /**
     * Returns information describing connections that are currently being used.
     */
    private String getBusyConnectionInformation(boolean toLogger) {
        
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {

          return busyList.getBusyConnectionInformation(toLogger);
          
        } finally {
            lock.unlock();
        }
    }
        
}

