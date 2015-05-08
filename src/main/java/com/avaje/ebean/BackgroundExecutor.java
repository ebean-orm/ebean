package com.avaje.ebean;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background thread pool service for executing of tasks asynchronously.
 * <p>
 * This service is used internally by Ebean for executing background tasks such
 * as the {@link Query#findFutureList()} and also for executing background tasks
 * periodically.
 * </p>
 * <p>
 * This service has been made available so you can use it for your application
 * code if you want. It can be useful for some server caching implementations
 * (background population and trimming of the cache etc).
 * </p>
 * 
 * @author rbygrave
 */
public interface BackgroundExecutor {

  /**
   * Execute a task in the background.
   */
  void execute(Runnable r);

  /**
   * Execute a task periodically with a fixed delay between each execution.
   * <p>
   * For example, execute a runnable every minute.
   * </p>
   * <p>
   * The delay is the time between executions no matter how long the task took.
   * That is, this method has the same behaviour characteristics as
   * {@link ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)}
   * </p>
   */
  void executePeriodically(Runnable r, long delay, TimeUnit unit);
}
