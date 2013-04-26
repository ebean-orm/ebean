package com.avaje.ebeaninternal.server.lib.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.config.GlobalProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 * This is a pool of threads which can be assigned work.
 * <p>
 * The Pool will automatically grow as required up to its maximum pool size. The
 * Pool will be automatically shrink by trimming threads that have been idle for
 * some time.
 * </p>
 */
public class ThreadPool {

  private static final Logger logger = LoggerFactory.getLogger(ThreadPool.class);

  /**
   * The max idle time used to trim idle threads from the pool.
   */
  private long maxIdleTime;

  /**
   * The name of the pool
   */
  private String poolName;

  /**
   * The initial pool size.
   */
  private int minSize;

  /**
   * Whether or not the threads are going to be Daemon threads.
   */
  private boolean isDaemon;

  /**
   * The priority or the threads. Can be null, in which case the threads have
   * the default priority.
   */
  private Integer threadPriority;

  /**
   * Incrementing int for thread name. NB: currentThreadCount will go up and
   * down as the pool grows and shrinks.
   */
  private int uniqueThreadID;

  /**
   * The maximum number of threads to grow to. Hitting this limit will have
   * performance ramifications.
   */
  private int maxSize = 100;

  /**
   * Flag that the pool should terminate all the threads and stop.
   */
  private boolean stopThePool;

  /**
   * Flag to indicate that the pool is being shutdown.
   */
  private boolean isStopping;

  /**
   * List of PooledThread that are free for work.
   */
  private Vector<PooledThread> freeList = new Vector<PooledThread>();

  /**
   * List of PooledThread that are busy.
   */
  private Vector<PooledThread> busyList = new Vector<PooledThread>();

  /**
   * List holding queued work.
   */
  private Vector<Work> workOverflowQueue = new Vector<Work>();

  /**
   * Create and return a ThreadPool reading configuration from GlobalProperties.
   */
  public static ThreadPool createThreadPool(String poolName) {

    int min = GlobalProperties.getInt("threadpool." + poolName + ".min", 0);
    int max = GlobalProperties.getInt("threadpool." + poolName + ".max", 100);

    int defaultIdleTime = GlobalProperties.getInt("threadpool.idletime", 60);
    long idle = 1000 * GlobalProperties.getInt("threadpool." + poolName + ".idletime", defaultIdleTime);

    Integer priority = null;
    String threadPriority = GlobalProperties.get("threadpool." + poolName + ".priority", null);
    if (threadPriority != null) {
      priority = new Integer(threadPriority);
    }

    return new ThreadPool(poolName, true, priority, min, max, idle);
  }
  
  public ThreadPool(String poolName, boolean isDaemon, Integer threadPriority, int minSize, int maxSize, long maxIdleMillis) {
    this(poolName, isDaemon, threadPriority);
    this.minSize = minSize;
    this.maxSize = maxSize;
    this.maxIdleTime = maxIdleMillis;
  }
  
  /**
   * Create the ThreadPool.
   */
  public ThreadPool(String poolName, boolean isDaemon, Integer threadPriority) {

    this.poolName = poolName;
    this.isDaemon = isDaemon;
    this.threadPriority = threadPriority;
  }

  /**
   * Return true if the pool is shutting down.
   */
  public boolean isStopping() {
    synchronized (freeList) {
      return isStopping;
    }
  }

  /**
   * Return the name of the thread pool.
   */
  public String getName() {
    return poolName;
  }

  /**
   * Set the minimum size the pool should try to maintain.
   */
  public void setMinSize(int minSize) {
    if (minSize > 0) {
      if (minSize > maxSize) {
        this.maxSize = minSize;
      }
      this.minSize = minSize;
      maintainPoolSize();
    }
  }

  /**
   * Return the minimum size the pool should maintain.
   */
  public int getMinSize() {
    return minSize;
  }

  /**
   * Set the maximum size the pool should grow to.
   */
  public void setMaxSize(int maxSize) {
    if (maxSize > 0) {
      if (minSize > maxSize) {
        minSize = maxSize;
      }
      this.maxSize = maxSize;
      maintainPoolSize();
    }
  }

  /**
   * Return the maximum size this pool can grow to.
   */
  public int getMaxSize() {
    return maxSize;
  }

  /**
   * Return the total number of busy and free threads in the pool.
   */
  public int size() {
    return busyList.size() + freeList.size();
  }

  /**
   * Return the number of currently busy threads.
   */
  public int getBusyCount() {
    return busyList.size();
  }

  /**
   * Assign a task to the thread pool, specifing the options to wait or queue
   * the task if the pool is fully busy and can't grow.
   * <p>
   * When the pool is fully busy...
   * </p>
   * <p>
   * addToQueue=true -> work is added to queue, returns false<br>
   * addToQueue=false -> work is not done or queued, returns false<br>
   * </p>
   * 
   * @param work
   *          the runnable work to do.
   * @param addToQueueIfFull
   *          If the pool is maxed out and this is true then it queues the
   *          Runnable.
   */
  public boolean assign(Runnable work, boolean addToQueueIfFull) {

    if (stopThePool) {
      throw new RuntimeException("Pool is stopping... no more work please.");
    }

    Work runWork = new Work(work);

    // get the next available thread in the pool (block)
    PooledThread thread = getNextAvailableThread();
    if (thread != null) {
      // assign the work to that thread
      busyList.add(thread);
      thread.assignWork(runWork);
      return true;

    } else {
      if (addToQueueIfFull) {
        runWork.setEnterQueueTime(System.currentTimeMillis());
        workOverflowQueue.add(runWork);
      }
      return false;
    }
  }

  /**
   * Remove the thread from the pool. The thread should be stopped before it is
   * removed.
   */
  protected void removeThread(PooledThread thread) {
    synchronized (freeList) {
      busyList.remove(thread);
      freeList.remove(thread);
      freeList.notify();

      if (logger.isTraceEnabled()) {
        logger.trace("PooledThread stopped [" + getName() + "]");
      }
    }
  }

  /**
   * fired when a Thread from the pool has finished, and can be put back into
   * the pool.
   */
  protected void returnThread(PooledThread thread) {

    synchronized (freeList) {

      // deregister from the busyList
      busyList.remove(thread);

      if (!workOverflowQueue.isEmpty()) {
        // get the first bit of work off the queue
        Work queuedWork = (Work) workOverflowQueue.remove(0);

        // work out the queue time and counts etc
        queuedWork.setExitQueueTime(System.currentTimeMillis());
        busyList.add(thread);
        thread.assignWork(queuedWork);

      } else {
        if (logger.isTraceEnabled()) {
          logger.trace("returnThread - add to freeList [" + thread + "]");
        }
        // put the thread back onto the available list
        freeList.add(thread);
        // tell shutdown() one has returned
        freeList.notify();
      }
    }
  }

  /**
   * Get the next available thread. Block until thread is available. NB: The
   * dispatcher is blocked but work can still be assigned to the dispatcher in a
   * non-blocking way
   */
  private PooledThread getNextAvailableThread() {

    synchronized (freeList) {
      if (!freeList.isEmpty()) {
        return (PooledThread) freeList.remove(0);
      }
      if (size() < maxSize) {
        return growPool(true);
      }
      return null;
    }
  }

  /**
   * Return an Iterator of PooledThread that are currently running. You should
   * only use this for display. Use the getPooledThread() or interrupt() methods
   * to interrupt a particular thread.
   * 
   * @return an Iterator of busy PooledThread's.
   */
  public Iterator<PooledThread> getBusyThreads() {
    synchronized (freeList) {
      return busyList.iterator();
    }
  }
  
  /**
   * Shutdown the threadpool stopping all the threads. This will wait until any
   * busy threads have finished their assigned work.
   */
  public void shutdown() {
 
    synchronized (freeList) {
      if (isStopping) {
        logger.debug("already shutting down");
      }
      isStopping = true;

      if (logger.isDebugEnabled()) {
        logger.debug("ThreadPool [" + poolName + "] Shutting down; threadCount[" + size()+ "] busyCount[" + getBusyCount() + "]");
      }
      stopThePool = true;

      while (!freeList.isEmpty()) {
        PooledThread thread = (PooledThread) freeList.remove(0);
        thread.stop();
      }

      try {
        while (getBusyCount() > 0) {

          String msg = "ThreadPool [" + poolName + "] has [" + getBusyCount()+ "] busy threads, waiting for those to finish.";
          logger.info(msg);

          Iterator<PooledThread> busyThreads = getBusyThreads();
          while (busyThreads.hasNext()) {
            PooledThread busyThread = (PooledThread) busyThreads.next();
            String busymsg = "Busy thread [" + busyThread.getName() + "] work["+ busyThread.getWork() + "]";
            logger.info(busymsg);
          }

          freeList.wait();
          PooledThread thread = (PooledThread) freeList.remove(0);
          logger.debug("wait finished on thread[" + thread.getName() + "]");
          if (thread != null) {
            thread.stop();
          }
        }

      } catch (InterruptedException e) {
        logger.error("Error during threadpool shutdown", e);
      }
    }
  }

  /**
   * Trim or grow the pool leaving at least min free.
   */
  protected void maintainPoolSize() {
    synchronized (freeList) {
      if (isStopping) {
        // don't bother as the pool is shutting down
        return;
      }

      int numToStop = size() - minSize;
      if (numToStop > 0) {
        // should trim idle threads as we are over the minSize
        long usedAfter = System.currentTimeMillis() - maxIdleTime;
        ArrayList<PooledThread> stopList = new ArrayList<PooledThread>();
        Iterator<PooledThread> it = freeList.iterator();
        while (it.hasNext() && numToStop > 0) {
          PooledThread thread = (PooledThread) it.next();
          if (thread.getLastUsedTime() < usedAfter) {
            stopList.add(thread);
            numToStop--;
          }
        }
        Iterator<PooledThread> stopIt = stopList.iterator();
        while (stopIt.hasNext()) {
          PooledThread thread = (PooledThread) stopIt.next();
          if (logger.isDebugEnabled()) {
            logger.debug("trimming pool - stopping thread "+thread);
          }
          thread.stop();
        }
      }
      int numToAdd = minSize - size();
      if (numToAdd > 0) {
        // should add some more to the pool
        for (int i = 0; i < numToAdd; i++) {
          growPool(false);
        }
      }
    }
  }

  /**
   * Interrupt a named thread that is currently busy.
   * <p>
   * Returns the thread that was interrupted or null if the thread was not
   * found. If the thread was interrupted then it will automatically be stopped
   * and removed from the pool.
   * </p>
   * <p>
   * Note that it may take some time to actually interrupt the thread so an
   * immediate test to see if the thread stopped will probably be wrong.
   * 
   * <pre>
   * <code>
   * ThreadPool test = ThreadPoolManager.getThreadPool("test");
   * PooledThread pt = test.interrupt("test.1");
   * if (pt == null) {
   *      // the thread was not found, perhaps finished?
   * } else {
   *      // give interrupt a little time to execute
   *      Thread.sleep(1000);
   *      boolean hasStopped = pt.isStopped();
   *      //..
   * }
   * </code>
   * </pre>
   * 
   * </p>
   * 
   * @return the thread that was interrupted
   */
  public PooledThread interrupt(String threadName) {
    PooledThread thread = getBusyThread(threadName);
    if (thread != null) {
      thread.interrupt();
      return thread;
    }
    return null;
  }

  /**
   * Find a thread using its name from the busy list. Returns null if the thread
   * is not found in the busy list.
   */
  public PooledThread getBusyThread(String threadName) {
    synchronized (freeList) {
      Iterator<PooledThread> it = getBusyThreads();
      while (it.hasNext()) {
        PooledThread pt = (PooledThread) it.next();
        if (pt.getName().equals(threadName)) {
          return pt;
        }
      }
      return null;
    }
  }

  /**
   * Grow the pool with the option of either putting it on the available list,
   * or returning it.
   */
  private PooledThread growPool(boolean andReturn) {

    synchronized (freeList) {

      String threadName = poolName + "." + uniqueThreadID++;
      PooledThread bgw = new PooledThread(this, threadName, isDaemon, threadPriority);
      bgw.start();

      if (logger.isDebugEnabled()) {
        logger.debug("ThreadPool grow created [" + threadName + "] size[" + size() + "]");
      }
      if (andReturn) {
        return bgw;
      } else {
        freeList.add(bgw);
        return null;
      }
    }
  }

  /**
   * Return the maximum amount of time in millis that Threads can be idle before
   * they are trimmed.
   */
  public long getMaxIdleTime() {
    return maxIdleTime;
  }

  /**
   * Set the maxiumium amount of time in millis that Threads can be idle before
   * they are trimed.
   */
  public void setMaxIdleTime(long maxIdleTime) {
    this.maxIdleTime = maxIdleTime;
  }

}
