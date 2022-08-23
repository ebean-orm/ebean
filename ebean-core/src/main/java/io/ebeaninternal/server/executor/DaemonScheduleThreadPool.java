package io.ebeaninternal.server.executor;

import io.ebeaninternal.api.CoreLog;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.*;

/**
 * Daemon based ScheduleThreadPool.
 */
public final class DaemonScheduleThreadPool extends ScheduledThreadPoolExecutor {

  private static final System.Logger log = CoreLog.log;

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
        log.log(DEBUG, "Already shutdown threadPool {0}", namePrefix);
        return;
      }
      try {
        log.log(TRACE, "shutting down threadPool {0}", namePrefix);
        super.shutdown();
        if (!super.awaitTermination(shutdownWaitSeconds, TimeUnit.SECONDS)) {
          log.log(INFO, "Shutdown wait timeout exceeded. Terminating running threads for {0}", namePrefix);
          super.shutdownNow();
        }
        log.log(TRACE, "shutdown complete for threadPool {0}", namePrefix);
      } catch (Exception e) {
        log.log(ERROR, "Error during shutdown of threadPool " + namePrefix, e);
        e.printStackTrace();
      }
    } finally {
      lock.unlock();
    }
  }
}
