package io.ebeaninternal.server.query;

import io.ebean.FutureMap;
import io.ebean.Query;
import io.ebean.Transaction;
import jakarta.persistence.PersistenceException;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Default implementation for FutureMap.
 */
public final class QueryFutureMap<K, T> extends BaseFuture<Map<K, T>> implements FutureMap<K, T> {

  private final CallableQueryMap<K, T> call;

  public QueryFutureMap(CallableQueryMap<K, T> call) {
    super(new FutureTask<>(call));
    this.call = call;
  }

  public FutureTask<Map<K, T>> futureTask() {
    return futureTask;
  }

  public Transaction transaction() {
    return call.transaction;
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
  public Map<K, T> getUnchecked() {
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
  public Map<K, T> getUnchecked(long timeout, TimeUnit unit) throws TimeoutException {
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
