package io.ebean;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Background executor service for executing of tasks asynchronously.
 * <p>
 * This service can be used to execute tasks in the background.
 * <p>
 * This service is managed by Ebean and will perform a clean shutdown
 * waiting for background tasks to complete with a default 30 second
 * timeout. Shutdown occurs prior to DataSource shutdown.
 * <p>
 * This also propagates MDC context from the current thread to the
 * background task if defined.
 */
public interface BackgroundExecutor {

  /**
   * Execute a callable task in the background returning the Future.
   */
  <T> Future<T> submit(Callable<T> task);

  /**
   * Execute a runnable task in the background returning the Future.
   */
  Future<?> submit(Runnable task);

  /**
   * Execute a task in the background. Effectively the same as
   * {@link BackgroundExecutor#submit(Runnable)} but returns void.
   */
  void execute(Runnable task);

  /**
   * Execute a task periodically with a fixed delay between each execution.
   * <p>
   * For example, execute a runnable every minute.
   * <p>
   * The delay is the time between executions no matter how long the task took.
   * That is, this method has the same behaviour characteristics as
   * {@link ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)}
   */
  void executePeriodically(Runnable task, long delay, TimeUnit unit);

  /**
   * Execute a task periodically additionally with an initial delay different from delay.
   */
  void executePeriodically(Runnable task, long initialDelay, long delay, TimeUnit unit);

  /**
   * Schedules a Runnable for one-shot action that becomes enabled after the given delay.
   *
   * @return a ScheduledFuture representing pending completion of the task and
   *         whose get() method will return null upon completion
   */
  ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit);

  /**
   * Schedules a Callable for one-shot action that becomes enabled after the given delay.
   *
   * @return a ScheduledFuture that can be used to extract result or cancel
   */
  <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit);

}
