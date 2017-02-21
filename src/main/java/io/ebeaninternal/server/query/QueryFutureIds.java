package io.ebeaninternal.server.query;

import io.ebean.FutureIds;
import io.ebean.Query;
import io.ebean.Transaction;

import java.util.List;
import java.util.concurrent.FutureTask;

/**
 * Default implementation of FutureIds.
 */
public class QueryFutureIds<T> extends BaseFuture<List<Object>> implements FutureIds<T> {

  private final CallableQueryIds<T> call;

  public QueryFutureIds(CallableQueryIds<T> call) {
    super(new FutureTask<>(call));
    this.call = call;
  }

  public FutureTask<List<Object>> getFutureTask() {
    return futureTask;
  }

  public Transaction getTransaction() {
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

}
