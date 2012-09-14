package com.avaje.ebeaninternal.server.lib.thread;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A thread that belongs to a ThreadPool. It will return to the Threadpool when
 * it has finished its assigned task.
 */
public class PooledThread implements Runnable {

	private static final Logger logger = Logger.getLogger(PooledThread.class.getName());
	
    /**
     * Create the PooledThread.
     */
    protected PooledThread(ThreadPool threadPool, String name, boolean isDaemon,
            Integer threadPriority) {

        this.name = name;
        this.threadPool = threadPool;
        this.lastUsedTime = System.currentTimeMillis();

        thread = new Thread(this, name);
        thread.setDaemon(isDaemon);

        if (threadPriority != null) {
            thread.setPriority(threadPriority.intValue());
        }
        //thread.start();
    }
    
    protected void start() {
    	thread.start();
    }
    
    /**
     * Assign work to this thread. The thread will notify the listener when it
     * has finished the work.
     */
    public boolean assignWork(Work work) {
        synchronized (workMonitor) {
            this.work = work;
            workMonitor.notifyAll();
        }
        return true;
    }

    /**
     * process any assigned work until stopped or interrupted.
     */
    public void run() {
        // process assigned work until we receive a shutdown signal
        synchronized (workMonitor) {
            while (!isStopping) {
                try {
                    if (work == null) {
                        workMonitor.wait();
                    }
                } catch (InterruptedException e) {
                }
                doTheWork();
            }
        }
        
        // Tell stop() we have shut ourselves down successfully
        synchronized (threadMonitor) {
            threadMonitor.notifyAll();
        }
        //Log.debug("PooledThread [" + getName() + "] finished ");
        isStopped = true;
    }

    /**
     * Actually do the work and gather the appropriate measures.
     */
    private void doTheWork() {
        if (isStopping){
            return;
        }

        long startTime = System.currentTimeMillis();
        if (work == null) {
            // probably shutting down the thread

        } else {
            try {
                work.setStartTime(startTime);
                work.getRunnable().run();

            } catch (Throwable ex) {
				logger.log(Level.SEVERE, null, ex);

                if (wasInterrupted) {
                    this.isStopping = true;
                    threadPool.removeThread(this);
                    logger.info("PooledThread [" + name + "] removed due to interrupt");
                    try {
                        thread.interrupt();
                    } catch (Exception e){
                    	String msg = "Error interrupting PooledThead["+name+"]";
        				logger.log(Level.SEVERE, msg, e);
                    }
                    return;
                }
            }
        }
        lastUsedTime = System.currentTimeMillis();
        totalWorkCount++;
        totalWorkExecutionTime = totalWorkExecutionTime + lastUsedTime - startTime;
        this.work = null;
        threadPool.returnThread(this);

    }

    /**
     * Try to interrupt the thread.
     * <p>
     * If the Thread was interrupted then it will be removed from the pool.
     * </p>
     */
    public void interrupt() {

        // set a flag so doTheWork knows that it was interrupted 
        // and removes rather than returns
        wasInterrupted = true;
        try {
            thread.interrupt();
            
        } catch (SecurityException ex) {
            wasInterrupted = false;
            throw ex;
        }
    }
    
    /**
     * Returns true if the thread has finished.
     */
    public boolean isStopped() {
        return isStopped;
    }
    
    /**
     * Stop the thread relatively nicely. It will wait a maximum of 10 seconds
     * for it to complete any existing work.
     */
    protected void stop() {
        isStopping = true;
        
        synchronized (threadMonitor) {
            
            assignWork(null);
            //trace("stop assigned null work...");
            try {
                threadMonitor.wait(10000);
            } catch (InterruptedException e) {
                ;
            }
            
        }

        thread = null;
        threadPool.removeThread(this);
    }

    /**
     * return the name of the thread.
     */
    public String getName() {
        return name;
    }
    /**
     * Returns the currently executing work, otherwise null.
     */
    public Work getWork() {
        return work;
    }

    /**
     * The total number of jobs this thread has run.
     */
    public int getTotalWorkCount() {
        return totalWorkCount;
    }

    /**
     * The total time for performing all assigned work.
     */
    public long getTotalWorkExecutionTime() {
        return totalWorkExecutionTime;
    }
    
    /**
     * Returns the time this thread was last used.
     */
    public long getLastUsedTime() {
        return lastUsedTime;
    }

    /**
     * Flag to indicate that the thread was interrupted.
     */
    private boolean wasInterrupted = false;

    /**
     * The time the thread was last used.
     */
    private long lastUsedTime;

    /**
     * The work to run
     */
    private Work work = null;

    /**
     * Set to indicate the thread is stopping.
     */
    private boolean isStopping = false;

    /**
     * Set when the thread has stopped.
     */
    private boolean isStopped = false;
    /**
     * The background thread.
     */
    private Thread thread = null;

    /**
     * The pool this worker belongs to.
     */
    private ThreadPool threadPool;

    /**
     * The name of the Thread
     */
    private String name = null;

    /**
     * The thread synchronization object.
     */
    private Object threadMonitor = new Object();

    /**
     * The monitor for work notification.
     */
    private Object workMonitor = new Object();

    /**
     * Total number of work performed.
     */
    private int totalWorkCount = 0;

    /**
     * Total work execution time.
     */
    private long totalWorkExecutionTime = 0;

}
