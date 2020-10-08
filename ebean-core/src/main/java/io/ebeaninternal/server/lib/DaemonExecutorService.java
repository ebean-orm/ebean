package io.ebeaninternal.server.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A "CachedThreadPool" based on Daemon threads.
 * <p>
 * The Threads are created as needed and once idle live for 60 seconds.
 */
public final class DaemonExecutorService {

  private static final Logger logger = LoggerFactory.getLogger(DaemonExecutorService.class);

  private final String namePrefix;

  private final int shutdownWaitSeconds;

  private final ExecutorService service;

  /**
   * Construct the DaemonThreadPool.
   *
   * @param shutdownWaitSeconds the time in seconds allowed for the pool to shutdown nicely. After
   *                            this the pool is forced to shutdown.
   */
  public DaemonExecutorService(int shutdownWaitSeconds, String namePrefix) {
    this.service = Executors.newCachedThreadPool(new DaemonThreadFactory(namePrefix));
    this.shutdownWaitSeconds = shutdownWaitSeconds;
    this.namePrefix = namePrefix;
  }

  /**
   * Execute the Runnable.
   */
  public void execute(Runnable runnable) {
    service.execute(runnable);
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
      if (service.isShutdown()) {
        logger.debug("DaemonExecutorService[{}] already shut down", namePrefix);
        return;
      }
      try {
        logger.debug("DaemonExecutorService[{}] shutting down...", namePrefix);
        service.shutdown();
        if (!service.awaitTermination(shutdownWaitSeconds, TimeUnit.SECONDS)) {
          logger.info("DaemonExecutorService[{}] shut down timeout exceeded. Terminating running threads.", namePrefix);
          service.shutdownNow();
        }

      } catch (Exception e) {
        logger.error("Error during shutdown of DaemonThreadPool[" + namePrefix + "]", e);
        e.printStackTrace();
      }
    }
  }

}
