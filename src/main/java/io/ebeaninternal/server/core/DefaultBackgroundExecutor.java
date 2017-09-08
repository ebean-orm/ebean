package io.ebeaninternal.server.core;

import io.ebeaninternal.api.SpiBackgroundExecutor;
import io.ebeaninternal.server.lib.DaemonExecutorService;
import io.ebeaninternal.server.lib.DaemonScheduleThreadPool;
import org.slf4j.MDC;

import java.util.Map;
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
  @Override
  public void execute(Runnable r) {
    final Map<String, String> map = MDC.getCopyOfContextMap();

    if (map == null) {
      pool.execute(r);
    } else {
      pool.execute(() -> {
        MDC.setContextMap(map);
        try {
          r.run();
        } finally {
          MDC.clear();
        }
      });
    }
  }

  @Override
  public void executePeriodically(Runnable r, long delay, TimeUnit unit) {
    final Map<String, String> map = MDC.getCopyOfContextMap();

    if (map == null) {
      schedulePool.scheduleWithFixedDelay(r, delay, delay, unit);
    } else {
      schedulePool.scheduleWithFixedDelay(() -> {
        MDC.setContextMap(map);
        try {
          r.run();
        } finally {
          MDC.clear();
        }
      }, delay, delay, unit);
    }
  }

  @Override
  public void shutdown() {
    pool.shutdown();
    schedulePool.shutdown();
  }

}
