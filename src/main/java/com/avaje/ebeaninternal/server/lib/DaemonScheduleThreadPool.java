package com.avaje.ebeaninternal.server.lib;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Daemon based ScheduleThreadPool.
 * <p>
 * Uses Daemon threads and hooks into shutdown event.
 * </p>
 * 
 * @author rbygrave
 */
public final class DaemonScheduleThreadPool extends ScheduledThreadPoolExecutor {

  private static final Logger logger = LoggerFactory.getLogger(DaemonScheduleThreadPool.class);

  private final String namePrefix;

  private int shutdownWaitSeconds;

  /**
   * Construct the DaemonScheduleThreadPool.
   */
  public DaemonScheduleThreadPool(int coreSize, int shutdownWaitSeconds, String namePrefix) {
    
    super(coreSize, new DaemonThreadFactory(namePrefix));
    this.namePrefix = namePrefix;
    this.shutdownWaitSeconds = shutdownWaitSeconds;
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
        logger.debug("DaemonScheduleThreadPool {} already shut down", namePrefix);
        return;
      }
      try {
        logger.debug("DaemonScheduleThreadPool {} shutting down...", namePrefix);
        super.shutdown();
        if (!super.awaitTermination(shutdownWaitSeconds, TimeUnit.SECONDS)) {
          logger.info("DaemonScheduleThreadPool shut down timeout exceeded. Terminating running threads.");
          super.shutdownNow();
        }

      } catch (Exception e) {
        logger.error("Error during shutdown of " + namePrefix, e);
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
