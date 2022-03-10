package io.ebeaninternal.server.executor;

import io.avaje.lang.NonNullApi;
import io.ebean.config.BackgroundExecutorWrapper;
import io.ebeaninternal.api.SpiBackgroundExecutor;

import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the BackgroundExecutor.
 */
@NonNullApi
public final class DefaultBackgroundExecutor implements SpiBackgroundExecutor {

  protected static final Logger logger = LoggerFactory.getLogger("io.ebean.BackgroundExecutor");
  
  private final ScheduledExecutorService schedulePool;
  private final DaemonExecutorService pool;
  private final BackgroundExecutorWrapper wrapper;

  /**
   * Construct the default implementation of BackgroundExecutor.
   */
  public DefaultBackgroundExecutor(int schedulePoolSize, int shutdownWaitSeconds, String namePrefix, BackgroundExecutorWrapper wrapper) {
    this.schedulePool = new DaemonScheduleThreadPool(schedulePoolSize, shutdownWaitSeconds, namePrefix + "-periodic-");
    this.pool = new DaemonExecutorService(shutdownWaitSeconds, namePrefix);
    this.wrapper = wrapper;
    logger.debug("Created backgroundExecutor {} (schedulePoolSize={}, shutdownWaitSeconds={})", namePrefix, schedulePoolSize, shutdownWaitSeconds);
  }

  /**
   * Wrap the task with MDC context if defined.
   */
  <T> Callable<T> wrap(Callable<T> task) {
    if (wrapper == null) {
      return clock(task);
    } else {
      return wrapper.wrap(clock(task));
    }
  }

  /**
   * Wrap the task with MDC context if defined.
   */
  Runnable wrap(Runnable task) {
    if (wrapper == null) {
      return clock(task);
    } else {
      return wrapper.wrap(clock(task));
    }
  }
  
  private <T> Callable<T> clock(Callable<T> task) {
    if (logger.isTraceEnabled()) {
      long queued = System.nanoTime();
      logger.trace("Queued {}", task);
      return () -> {
        long start = System.nanoTime();
        logger.trace("Start {} (delay time {} us)", task, (start - queued) / 1000L);
        T ret = task.call();
        logger.trace("Stop {} (exec time {} us)", task, (System.nanoTime() - start) / 1000L);
        return ret;
      };
    } else {
      return task;
    }
  }
  
  private Runnable clock(Runnable task) {
    if (logger.isTraceEnabled()) {
      long queued = System.nanoTime();
      logger.trace("Queued {}", task);
      return () -> {
        long start = System.nanoTime();
        logger.trace("Start {} (delay time {} us)", task, (start - queued) / 1000L);
        task.run();
        logger.trace("Stop {} (exec time {} us)", task, (System.nanoTime() - start) / 1000L);
      };
    } else {
      return task;
    }
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return pool.submit(wrap(task));
  }

  /**
   * Execute a Runnable using a background thread.
   */
  @Override
  public Future<?> submit(Runnable task) {
    return pool.submit(wrap(task));
  }

  @Override
  public void execute(Runnable task) {
    submit(() -> {
      try {
        task.run();
      } catch (Throwable t) {
        logger.error("Error while executing the task {}", task, t);
      }
    });
  }

  @Override
  public void executePeriodically(Runnable task, long delay, TimeUnit unit) {
    schedulePool.scheduleWithFixedDelay(wrap(task), delay, delay, unit);
  }

  @Override
  public void executePeriodically(Runnable task, long initialDelay, long delay, TimeUnit unit) {
    schedulePool.scheduleWithFixedDelay(wrap(task), initialDelay, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
    return schedulePool.scheduleWithFixedDelay(wrap(task), initialDelay, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long delay, TimeUnit unit) {
    return schedulePool.scheduleAtFixedRate(wrap(task), initialDelay, delay, unit);
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
    return schedulePool.schedule(wrap(task), delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
    return schedulePool.schedule(wrap(task), delay, unit);
  }

  @Override
  public void shutdown() {
    logger.trace("Shutting down backgroundExecutor");
    schedulePool.shutdown();
    pool.shutdown();
    logger.debug("BackgroundExecutor stopped");
  }

}
