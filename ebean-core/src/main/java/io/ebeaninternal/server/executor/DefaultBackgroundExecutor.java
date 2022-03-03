package io.ebeaninternal.server.executor;

import io.avaje.lang.NonNullApi;
import io.ebeaninternal.api.SpiBackgroundExecutor;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.*;

/**
 * The default implementation of the BackgroundExecutor.
 */
@NonNullApi
public final class DefaultBackgroundExecutor implements SpiBackgroundExecutor {

  private final ScheduledExecutorService schedulePool;
  private final DaemonExecutorService pool;

  /**
   * Construct the default implementation of BackgroundExecutor.
   */
  public DefaultBackgroundExecutor(int schedulePoolSize, int shutdownWaitSeconds, String namePrefix) {
    this.schedulePool = new DaemonScheduleThreadPool(schedulePoolSize, shutdownWaitSeconds, namePrefix + "-periodic-");
    this.pool = new DaemonExecutorService(shutdownWaitSeconds, namePrefix);
  }

  /**
   * Wrap the task with MDC context if defined.
   */
  <T> Callable<T> wrapMDC(Callable<T> task) {
    final Map<String, String> map = MDC.getCopyOfContextMap();
    if (map == null) {
      return task;
    } else {
      return () -> {
        MDC.setContextMap(map);
        try {
          return task.call();
        } finally {
          MDC.clear();
        }
      };
    }
  }

  /**
   * Wrap the task with MDC context if defined.
   */
  Runnable wrapMDC(Runnable task) {
    final Map<String, String> map = MDC.getCopyOfContextMap();
    if (map == null) {
      return task;
    } else {
      return () -> {
        MDC.setContextMap(map);
        try {
          task.run();
        } finally {
          MDC.clear();
        }
      };
    }
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return pool.submit(wrapMDC(task));
  }

  /**
   * Execute a Runnable using a background thread.
   */
  @Override
  public Future<?> submit(Runnable task) {
    return pool.submit(wrapMDC(task));
  }

  @Override
  public void execute(Runnable task) {
    submit(task);
  }

  @Override
  public void executePeriodically(Runnable task, long delay, TimeUnit unit) {
    schedulePool.scheduleWithFixedDelay(wrapMDC(task), delay, delay, unit);
  }

  @Override
  public void executePeriodically(Runnable task, long initialDelay, long delay, TimeUnit unit) {
    schedulePool.scheduleWithFixedDelay(wrapMDC(task), initialDelay, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
    return schedulePool.scheduleWithFixedDelay(wrapMDC(task), initialDelay, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long delay, TimeUnit unit) {
    return schedulePool.scheduleAtFixedRate(wrapMDC(task), initialDelay, delay, unit);
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
    return schedulePool.schedule(wrapMDC(task), delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
    return schedulePool.schedule(wrapMDC(task), delay, unit);
  }

  @Override
  public void shutdown() {
    schedulePool.shutdown();
    pool.shutdown();
  }

}
