package io.ebeaninternal.server.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Daemon based ScheduleThreadPool.
 * <p>
 * Uses Daemon threads and hooks into shutdown event.
 * </p>
 */
public final class DaemonScheduleThreadPool extends ScheduledThreadPoolExecutor {

  private static final Logger logger = LoggerFactory.getLogger(DaemonScheduleThreadPool.class);

  private final ReentrantLock lock = new ReentrantLock(false);

  private final String namePrefix;

  private final int shutdownWaitSeconds;

  /**
   * Construct the DaemonScheduleThreadPool.
   */
  public DaemonScheduleThreadPool(int coreSize, int shutdownWaitSeconds, String namePrefix) {
    super(coreSize, new DaemonThreadFactory(namePrefix));
    this.namePrefix = namePrefix;
    this.shutdownWaitSeconds = shutdownWaitSeconds;
  }

  /**
   * Shutdown this thread pool nicely if possible.
   * <p>
   * This will wait a maximum of 20 seconds before terminating any threads still
   * working.
   * </p>
   */
  @Override
  public void shutdown() {
    lock.lock();
    try {
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
    } finally {
      lock.unlock();
    }
  }
}
