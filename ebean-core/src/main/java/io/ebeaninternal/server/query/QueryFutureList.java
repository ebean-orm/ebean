package io.ebeaninternal.server.query;

import io.ebean.FutureList;
import io.ebean.Query;
import io.ebean.Transaction;

import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Default implementation for FutureList.
 */
public final class QueryFutureList<T> extends BaseFuture<List<T>> implements FutureList<T> {

  private final CallableQueryList<T> call;

  public QueryFutureList(CallableQueryList<T> call) {
    super(new FutureTask<>(call));
    this.call = call;
  }

  public FutureTask<List<T>> futureTask() {
    return futureTask;
  }

  @Override
  public Query<T> getQuery() {
    return call.query;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    call.query.cancel();
    return super.cancel(mayInterruptIfRunning);
  }

  @Override
  public List<T> getUnchecked() {
    try {
      return get();

    } catch (InterruptedException e) {
      // restore the interrupted status (so client can check for that)
      Thread.currentThread().interrupt();
      throw new PersistenceException(e);

    } catch (ExecutionException e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public List<T> getUnchecked(long timeout, TimeUnit unit) throws TimeoutException {
    try {
      return get(timeout, unit);

    } catch (InterruptedException e) {
      // restore the interrupted status (so client can check for that)
      Thread.currentThread().interrupt();
      throw new PersistenceException(e);

    } catch (ExecutionException e) {
      throw new PersistenceException(e);
    }
  }

}
