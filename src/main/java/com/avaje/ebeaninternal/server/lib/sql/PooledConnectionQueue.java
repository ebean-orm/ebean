package com.avaje.ebeaninternal.server.lib.sql;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.avaje.ebeaninternal.server.lib.sql.DataSourcePool.Status;

public class PooledConnectionQueue {

    private static final Logger logger = Logger.getLogger(PooledConnectionQueue.class.getName());
    
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
     * Main lock guarding all access 
     */
    private final ReentrantLock lock;
    
    /** 
     * Condition for threads waiting to take a connection 
     */
    private final Condition notEmpty;

    private int connectionId;

    private long waitTimeoutMillis;

    private long leakTimeMinutes;
    
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
                
        this.busyList = new BusyConnectionBuffer(50,20);
        this.freeList = new FreeConnectionBuffer(maxSize);
        
        this.lock = new ReentrantLock(true);
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
            freeList.setCapacity(maxSize);
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
    protected void returnPooledConnection(PooledConnection c) {
        
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {            
            if (!busyList.remove(c)) {
                logger.log(Level.SEVERE, "Connection [" + c + "] not found in BusyList? ");
            }
            if (c.getCreationTime() <= lastResetTime) {
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
                
                int freeSize = freeList.size();
                if (freeSize > 0){
                    // we have a free connection to return
                    return extractFromFreeList();
                } 
                
                if (busyList.size() < maxSize){
                    // grow the connection pool
                    PooledConnection c = pool.createConnectionForQueue(connectionId++);
                    int busySize = registerBusyConnection(c);
                    
                    String msg = "DataSourcePool [" + name + "] grow; id["+c.getName()+"] busy["+busySize+"] max["+maxSize+"]";
                    logger.info(msg);
                    
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
            logger.info("DataSourcePool [" + name + "] shutdown: "+status);
            
            closeFreeConnections(true);
        
            if (!busyList.isEmpty()) {
                String msg = "A potential connection leak was detected.  Busy connections: "+ busyList.size();
                logger.warning(msg);
                
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
            logger.info("Reseting DataSourcePool [" + name + "] "+status);
            lastResetTime = System.currentTimeMillis();

            closeFreeConnections(false);
            closeBusyConnections(leakTimeMinutes);

            String busyMsg = "Busy Connections:\r\n" + getBusyConnectionInformation();
            logger.info(busyMsg);

        } finally {
            lock.unlock();
        }
    }

    public void trim(int maxInactiveTimeSecs) throws SQLException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            trimInactiveConnections(maxInactiveTimeSecs);
            ensureMinimumConnections();
            
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Trim connections that have been not used for some time.
     */
    private int trimInactiveConnections(int maxInactiveTimeSecs) {
    
        int maxTrim = freeList.size() - minSize;
        if (maxTrim <= 0) {
            return 0;
        }
        
        int trimedCount = 0;
        long usedSince = System.currentTimeMillis() - (maxInactiveTimeSecs * 1000);
      
        // get a shallow copy to manipulate
        List<PooledConnection> freeListCopy = freeList.getShallowCopy();
        
        Iterator<PooledConnection> it = freeListCopy.iterator();
        while (it.hasNext()) {
            PooledConnection pc = it.next();
            if (pc.getLastUsedTime() < usedSince) {
                // trim this connection as it hasn't been used in a while
                trimedCount++;
                it.remove();
                pc.closeConnectionFully(true);
                if (trimedCount >= maxTrim) {
                    break;
                }
            }
        }
        
        if (trimedCount > 0) {
            
            // rebuild the free list from the trimmed copy
            freeList.setShallowCopy(freeListCopy);

            String msg = "DataSourcePool [" + name + "] trimmed [" + trimedCount + "] inactive connections. New size[" + totalConnections() + "]";
            logger.info(msg);
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
            while (!freeList.isEmpty()) {
                PooledConnection c = freeList.remove();
                logger.info("PSTMT Statistics: "+c.getStatistics());
                c.closeConnectionFully(logErrors);
            }
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

            long olderThanTime = System.currentTimeMillis() - (leakTimeMinutes*60000);

            List<PooledConnection> copy = busyList.getShallowCopy();
            for (int i = 0; i < copy.size(); i++) {
                PooledConnection pc = copy.get(i);
                if (pc.isLongRunning() || pc.getLastUsedTime() > olderThanTime) {
                    // PooledConnection has been used recently or
                    // expected to be longRunning so not closing...
                } else {
                    busyList.remove(pc);
                    closeBusyConnection(pc);
                }
            }
            
        } finally {
            lock.unlock();
        }
    }

    private void closeBusyConnection(PooledConnection pc) {
        try {
            String methodLine = pc.getCreatedByMethod();

            Date luDate = new Date();
            luDate.setTime(pc.getLastUsedTime());

            String msg = "DataSourcePool closing leaked connection? " + " name["
                    + pc.getName() + "] lastUsed[" + luDate + "] createdBy[" + methodLine
                    + "] lastStmt[" + pc.getLastStatement() + "]";

            logger.warning(msg);
            logStackElement(pc, "Possible Leaked Connection: ");
            
            System.out.println("CLOSING BUSY CONNECTION ??? "+pc);
            pc.close();

        } catch (SQLException ex) {
            // this should never actually happen
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    private void logStackElement(PooledConnection pc, String prefix) {
        StackTraceElement[] stackTrace = pc.getStackTrace();
        if (stackTrace != null){
            String s = Arrays.toString(stackTrace);
            String msg = prefix+" name["+pc.getName()+"] stackTrace: "+s;
            logger.warning(msg);
            // also send to syserr ... as the loggers get turned 
            // off early in JVM shutdown
            System.err.println(msg);
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

            if (toLogger) {
                logger.info("Dumping busy connections: (Use datasource.xxx.capturestacktrace=true  ... to get stackTraces)");
            }

            StringBuilder sb = new StringBuilder();
            
            List<PooledConnection> copy = busyList.getShallowCopy();
            for (int i = 0; i < copy.size(); i++) {
                PooledConnection pc = copy.get(i);
                if (toLogger) {
                    logger.info(pc.getDescription());
                    logStackElement(pc, "Busy Connection: ");

                } else {
                    sb.append(pc.getDescription()).append("\r\n");
                }
            }

            return sb.toString();

        } finally {
            lock.unlock();
        }
    }
        
}

