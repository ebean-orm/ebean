package com.avaje.ebeaninternal.server.lib;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Thread Pool based on Daemon threads.
 * 
 * @author rbygrave
 */
public final class DaemonThreadPool extends ThreadPoolExecutor {

  private static final Logger logger = LoggerFactory.getLogger(DaemonThreadPool.class);

  private final String namePrefix;

  private final int shutdownWaitSeconds;

  /**
   * Construct the DaemonThreadPool.
   * 
   * @param coreSize
   *          the core size of the thread pool.
   * @param keepAliveSecs
   *          the time in seconds idle threads are keep alive
   * @param shutdownWaitSeconds
   *          the time in seconds allowed for the pool to shutdown nicely. After
   *          this the pool is forced to shutdown.
   */
  public DaemonThreadPool(int coreSize, int maximumPoolSize, long keepAliveSecs, int shutdownWaitSeconds, String namePrefix) {
    
    super(coreSize, maximumPoolSize, keepAliveSecs, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DaemonThreadFactory(namePrefix));
    allowCoreThreadTimeOut(true);
    this.shutdownWaitSeconds = shutdownWaitSeconds;
    this.namePrefix = namePrefix;
  }

  /**
   * Register a shutdown hook with the JVM Runtime.
   */
  public void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new ShutdownHook());    
  }
  
  /**
   * Shutdown this thread pool nicely if possible.
   * <p>
   * This will wait a maximum of 20 seconds before terminating any threads still
   * working.
   * </p>
   */
  public void shutdown() {
    synchronized (this) {
      if (super.isShutdown()) {
        logger.debug("DaemonThreadPool[" + namePrefix + "] already shut down");
        return;
      }
      try {
        logger.debug("DaemonThreadPool[" + namePrefix + "] shutting down...");
        super.shutdown();
        if (!super.awaitTermination(shutdownWaitSeconds, TimeUnit.SECONDS)) {
          logger.info("DaemonThreadPool[" + namePrefix+ "] shut down timeout exceeded. Terminating running threads.");
          super.shutdownNow();
        }

      } catch (Exception e) {
        logger.error("Error during shutdown of DaemonThreadPool[" + namePrefix + "]", e);
        e.printStackTrace();
      }
    }
  }

  /**
   * Fired by the JVM Runtime shutdown.
   */
  private class ShutdownHook extends Thread {
    @Override
    public void run() {
      shutdown();
    }
  };
}
