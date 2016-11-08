package com.avaje.ebeaninternal.server.core;

import com.avaje.ebeaninternal.api.SpiBackgroundExecutor;
import com.avaje.ebeaninternal.server.lib.DaemonExecutorService;
import com.avaje.ebeaninternal.server.lib.DaemonScheduleThreadPool;

import java.util.concurrent.TimeUnit;

/**
 * The default implementation of the BackgroundExecutor.
 */
public class DefaultBackgroundExecutor implements SpiBackgroundExecutor {

  private final DaemonScheduleThreadPool schedulePool;

  private final DaemonExecutorService pool;

  /**
   * Construct the default implementation of BackgroundExecutor.
   */
  public DefaultBackgroundExecutor(int schedulePoolSize, int shutdownWaitSeconds, String namePrefix) {
    this.pool = new DaemonExecutorService(shutdownWaitSeconds, namePrefix);
    this.schedulePool = new DaemonScheduleThreadPool(schedulePoolSize, shutdownWaitSeconds, namePrefix + "-periodic-");
  }

  /**
   * Execute a Runnable using a background thread.
   */
  public void execute(Runnable r) {
    pool.execute(r);
  }

  public void executePeriodically(Runnable r, long delay, TimeUnit unit) {
    schedulePool.scheduleWithFixedDelay(r, delay, delay, unit);
  }

  public void shutdown() {
    pool.shutdown();
    schedulePool.shutdown();
  }

}
