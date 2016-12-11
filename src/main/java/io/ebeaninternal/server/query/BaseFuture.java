package io.ebeaninternal.server.query;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A base object for query Future objects.
 *
 * @param <T> the entity bean type
 * @author rbygrave
 */
abstract class BaseFuture<T> implements Future<T> {

  final FutureTask<T> futureTask;

  BaseFuture(FutureTask<T> futureTask) {
    this.futureTask = futureTask;
  }

  public boolean cancel(boolean mayInterruptIfRunning) {
    return futureTask.cancel(mayInterruptIfRunning);
  }

  public T get() throws InterruptedException, ExecutionException {
    return futureTask.get();
  }

  public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return futureTask.get(timeout, unit);
  }

  public boolean isCancelled() {
    return futureTask.isCancelled();
  }

  public boolean isDone() {
    return futureTask.isDone();
  }

}
