package io.ebeaninternal.server.executor;

import io.ebeaninternal.api.SpiBackgroundExecutor;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The default implementation of the BackgroundExecutor.
 */
public class DefaultBackgroundExecutor implements SpiBackgroundExecutor {

  private final ScheduledExecutorService executor;

  /**
   * Construct the default implementation of BackgroundExecutor.
   */
  public DefaultBackgroundExecutor(int schedulePoolSize, int shutdownWaitSeconds, String namePrefix) {
    this.executor = new DaemonScheduleThreadPool(schedulePoolSize, shutdownWaitSeconds, namePrefix);
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
    return executor.submit(wrapMDC(task));
  }

  /**
   * Execute a Runnable using a background thread.
   */
  @Override
  public Future<?> submit(Runnable task) {
    return executor.submit(wrapMDC(task));
  }

  @Override
  public void execute(Runnable task) {
    submit(task);
  }

  @Override
  public void executePeriodically(Runnable task, long delay, TimeUnit unit) {
    executor.scheduleWithFixedDelay(wrapMDC(task), delay, delay, unit);
  }

  @Override
  public void executePeriodically(Runnable task, long initialDelay, long delay, TimeUnit unit) {
    executor.scheduleWithFixedDelay(wrapMDC(task), initialDelay, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
    return executor.scheduleWithFixedDelay(wrapMDC(task), initialDelay, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long delay, TimeUnit unit) {
    return executor.scheduleAtFixedRate(wrapMDC(task), initialDelay, delay, unit);
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
    return executor.schedule(wrapMDC(task), delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
    return executor.schedule(wrapMDC(task), delay, unit);
  }

  @Override
  public void shutdown() {
    executor.shutdown();
  }

}
