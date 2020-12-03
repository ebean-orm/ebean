package io.ebeaninternal.server.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Daemon based ScheduleThreadPool.
 */
public final class DaemonScheduleThreadPool extends ScheduledThreadPoolExecutor {

  private static final Logger logger = LoggerFactory.getLogger(DaemonScheduleThreadPool.class);

  private final ReentrantLock lock = new ReentrantLock();

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
   * This will wait a maximum of shutdownWaitSeconds seconds before
   * terminating any threads still working.
   */
  @Override
  public void shutdown() {
    lock.lock();
    try {
      if (super.isShutdown()) {
        logger.debug("Already shutdown {}", namePrefix);
        return;
      }
      try {
        logger.trace("Shutting down {} ...", namePrefix);
        super.shutdown();
        if (!super.awaitTermination(shutdownWaitSeconds, TimeUnit.SECONDS)) {
          logger.info("Shutdown wait timeout exceeded. Terminating running threads for {}", namePrefix);
          super.shutdownNow();
        }
        logger.debug("Shutdown complete for {}", namePrefix);
      } catch (Exception e) {
        logger.error("Error during shutdown of " + namePrefix, e);
        e.printStackTrace();
      }
    } finally {
      lock.unlock();
    }
  }
}
