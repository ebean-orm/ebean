package io.ebeaninternal.server.executor;

import io.avaje.applog.AppLog;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.*;

/**
 * A "CachedThreadPool" based on Daemon threads.
 * <p>
 * The Threads are created as needed and once idle live for 60 seconds.
 */
final class DaemonExecutorService {

  private static final System.Logger logger = AppLog.getLogger(DaemonExecutorService.class);

  private final ReentrantLock lock = new ReentrantLock(false);
  private final String namePrefix;
  private final int shutdownWaitSeconds;
  private final ExecutorService service;

  DaemonExecutorService(int shutdownWaitSeconds, String namePrefix) {
    this.service = Executors.newCachedThreadPool(new DaemonThreadFactory(namePrefix));
    this.shutdownWaitSeconds = shutdownWaitSeconds;
    this.namePrefix = namePrefix;
  }

  <T> Future<T> submit(Callable<T> task) {
    return service.submit(task);
  }

  Future<?> submit(Runnable task) {
    return service.submit(task);
  }

  /**
   * Shutdown this thread pool nicely if possible.
   * <p>
   * This will wait a maximum of 20 seconds before terminating any threads still working.
   */
  void shutdown() {
    lock.lock();
    try {
      if (service.isShutdown()) {
        logger.log(DEBUG, "DaemonExecutorService[{0}] already shut down", namePrefix);
        return;
      }
      try {
        logger.log(DEBUG, "DaemonExecutorService[{0}] shutting down...", namePrefix);
        service.shutdown();
        if (!service.awaitTermination(shutdownWaitSeconds, TimeUnit.SECONDS)) {
          logger.log(INFO, "DaemonExecutorService[{0}] shut down timeout exceeded. Terminating running threads.", namePrefix);
          service.shutdownNow();
        }

      } catch (Exception e) {
        logger.log(ERROR, "Error during shutdown of DaemonThreadPool[" + namePrefix + "]", e);
        e.printStackTrace();
      }
    } finally {
      lock.unlock();
    }
  }

}
