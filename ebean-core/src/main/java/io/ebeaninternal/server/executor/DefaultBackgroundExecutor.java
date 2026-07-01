package io.ebeaninternal.server.executor;

import io.avaje.applog.AppLog;
import org.jspecify.annotations.NullMarked;
import io.ebean.config.BackgroundExecutorWrapper;
import io.ebeaninternal.api.SpiBackgroundExecutor;

import java.util.concurrent.*;

import static java.lang.System.Logger.Level.*;

/**
 * The default implementation of the BackgroundExecutor.
 */
@NullMarked
public final class DefaultBackgroundExecutor implements SpiBackgroundExecutor {

  private static final System.Logger log = AppLog.getLogger("io.ebean.BackgroundExecutor");

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
  }

  /**
   * Wrap the task with MDC context if defined.
   */
  <T> Callable<T> wrap(Callable<T> task) {
    if (wrapper == null) {
      return task;
    } else {
      return wrapper.wrap(task);
    }
  }

  /**
   * Wrap the task with MDC context if defined.
   */
  Runnable wrap(Runnable task) {
    if (wrapper == null) {
      return task;
    } else {
      return wrapper.wrap(task);
    }
  }


  /**
   * Decorates a runnable by adding an exception handler and some timing metrics.
   * This is used in methods that accepts a <code>Runnable</code> and return
   * either <code>void</code> or <code>ScheduledFuture</code>, as there is
   * normally no Future.get() call.
   * <p>
   * Note: When submitting a <code>Callable</code>, you must check
   * <code>Future.get()</code> for exceptions.
   */
  private Runnable logExceptions(Runnable task) {
    long queued = System.nanoTime();
    log.log(TRACE, "Queued {0}", task);
    return () -> {
      try {
        if (log.isLoggable(TRACE)) {
          long start = System.nanoTime();
          log.log(TRACE, "Start {0} (delay time {1} us)", task, (start - queued) / 1000L);
          task.run();
          log.log(TRACE, "Stop {0} (exec time {1} us)", task, (System.nanoTime() - start) / 1000L);
        } else {
          task.run();
        }
      } catch (Throwable t) {
        // log any exception here. Note they will not bubble up to the calling user
        // unless Future.get() is checked. (Which is almost never done on scheduled
        // background executions)
        log.log(ERROR, "Error while executing the task " + task, t);
        throw t;
      }
    };
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    // Note: No "logExceptions" as we expect Future.get() by the invoker
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
    submit(logExceptions(task));
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
    return schedulePool.scheduleWithFixedDelay(logExceptions(task), initialDelay, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long delay, TimeUnit unit) {
    return schedulePool.scheduleAtFixedRate(logExceptions(task), initialDelay, delay, unit);
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
    return schedulePool.schedule(logExceptions(task), delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
    // Note: No "logExceptions" as we expect Future.get() by the invoker
    return schedulePool.schedule(task, delay, unit);
  }

  @Override
  public void shutdown() {
    log.log(TRACE, "BackgroundExecutor shutting down");
    schedulePool.shutdown();
    pool.shutdown();
    log.log(DEBUG, "BackgroundExecutor stopped");
  }

}
