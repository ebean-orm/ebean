package io.ebean;

import org.jspecify.annotations.NullMarked;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
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
@NullMarked
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
   * Execute a task periodically with a given delay.
   *
   * @param task         the task to execute
   * @param initialDelay the time to delay first execution
   * @param delay        the delay between the termination of one
   *                     execution and the commencement of the next
   * @param unit         the time unit of the initialDelay and delay parameters
   * @return a ScheduledFuture representing pending completion of
   * the series of repeated tasks.  The future's {@link
   * Future#get() get()} method will never return normally,
   * and will throw an exception upon task cancellation or
   * abnormal termination of a task execution.
   */
  ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit);

  /**
   * Execute a task periodically with a given period.
   *
   * <p>If any execution of this task takes longer than its period, then
   * subsequent executions may start late, but will not concurrently
   * execute.
   *
   * @param task         the task to execute
   * @param initialDelay the time to delay first execution
   * @param period       the period between successive executions
   * @param unit         the time unit of the initialDelay and period parameters
   * @return a ScheduledFuture representing pending completion of
   * the series of repeated tasks.  The future's {@link
   * Future#get() get()} method will never return normally,
   * and will throw an exception upon task cancellation or
   * abnormal termination of a task execution.
   */
  ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit);

  /**
   * Schedules a Runnable for one-shot action that becomes enabled after the given delay.
   *
   * @return a ScheduledFuture representing pending completion of the task and
   * whose get() method will return null upon completion
   */
  ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit);

  /**
   * Schedules a Callable for one-shot action that becomes enabled after the given delay.
   *
   * @return a ScheduledFuture that can be used to extract result or cancel
   */
  <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit);

}
