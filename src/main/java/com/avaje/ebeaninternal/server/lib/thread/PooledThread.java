package com.avaje.ebeaninternal.server.lib.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread that belongs to a ThreadPool. It will return to the Threadpool when
 * it has finished its assigned task.
 */
public class PooledThread implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(PooledThread.class);

  /**
   * Flag to indicate that the thread was interrupted.
   */
  private boolean wasInterrupted;

  /**
   * The time the thread was last used.
   */
  private long lastUsedTime;

  /**
   * The work to run
   */
  private Work work;

  /**
   * Set to indicate the thread is stopping.
   */
  private boolean isStopping;

  /**
   * Set when the thread has stopped.
   */
  private boolean isStopped;
  /**
   * The background thread.
   */
  private Thread thread;

  /**
   * The pool this worker belongs to.
   */
  private ThreadPool threadPool;

  /**
   * The name of the Thread
   */
  private String name;

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
  private int totalWorkCount;

  /**
   * Total work execution time.
   */
  private long totalWorkExecutionTime;
  
  /**
   * Create the PooledThread.
   */
  protected PooledThread(ThreadPool threadPool, String name, boolean isDaemon, Integer threadPriority) {

    this.name = name;
    this.threadPool = threadPool;
    this.lastUsedTime = System.currentTimeMillis();

    thread = new Thread(this, name);
    thread.setDaemon(isDaemon);

    if (threadPriority != null) {
      thread.setPriority(threadPriority.intValue());
    }
  }

  public String toString() {
    return name;
  }

  protected void start() {
    thread.start();
  }

  /**
   * Assign work to this thread. The thread will notify the listener when it has
   * finished the work.
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
    if (logger.isTraceEnabled()) {
      logger.trace("PooledThread [" + getName() + "] finished ");
    }
    isStopped = true;
  }

  /**
   * Actually do the work and gather the appropriate measures.
   */
  private void doTheWork() {
    if (isStopping) {
      return;
    }

    long startTime = System.currentTimeMillis();
    if (work == null) {
      // probably shutting down the thread

    } else {
      try {
        if (logger.isTraceEnabled()) {
          logger.trace("start work "+work);
        }
        
        work.setStartTime(startTime);
        work.getRunnable().run();

        if (logger.isTraceEnabled()) {
          logger.trace("finished work "+work);
        }
        
      } catch (Throwable ex) {
        logger.error(null, ex);

        if (wasInterrupted) {
          this.isStopping = true;
          threadPool.removeThread(this);
          if (logger.isInfoEnabled()) {
            logger.info("PooledThread [" + name + "] removed due to interrupt");
          }
          try {
            thread.interrupt();
          } catch (Exception e) {
            logger.error("Error interrupting PooledThead[" + name + "]", e);
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
      if (logger.isTraceEnabled()) {
        logger.trace("interrupt()");
      }
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
   * Stop the thread relatively nicely. It will wait a maximum of 10 seconds for
   * it to complete any existing work.
   */
  protected void stop() {
    isStopping = true;

    synchronized (threadMonitor) {

      assignWork(null);
      if (logger.isTraceEnabled()) {
        logger.trace("stopping thread ["+name+"]");
      }
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

}
