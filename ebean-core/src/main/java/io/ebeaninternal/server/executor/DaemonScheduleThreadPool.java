package io.ebeaninternal.server.executor;

import io.ebeaninternal.api.CoreLog;
import org.slf4j.Logger;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Daemon based ScheduleThreadPool.
 */
public final class DaemonScheduleThreadPool extends ScheduledThreadPoolExecutor {

  private static final Logger log = CoreLog.log;

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
        log.debug("Already shutdown threadPool {}", namePrefix);
        return;
      }
      try {
        log.trace("shutting down threadPool {}", namePrefix);
        super.shutdown();
        if (!super.awaitTermination(shutdownWaitSeconds, TimeUnit.SECONDS)) {
          log.info("Shutdown wait timeout exceeded. Terminating running threads for {}", namePrefix);
          super.shutdownNow();
        }
        log.trace("shutdown complete for threadPool {}", namePrefix);
      } catch (Exception e) {
        log.error("Error during shutdown of threadPool " + namePrefix, e);
        e.printStackTrace();
      }
    } finally {
      lock.unlock();
    }
  }
}
