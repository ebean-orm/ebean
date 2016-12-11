package io.ebeaninternal.server.query;

import io.ebean.FutureRowCount;
import io.ebean.Query;
import io.ebean.Transaction;

import java.util.concurrent.FutureTask;

/**
 * Future implementation for the row count query.
 */
public class QueryFutureRowCount<T> extends BaseFuture<Integer> implements FutureRowCount<T> {

  private final CallableQueryRowCount<T> call;

  public QueryFutureRowCount(CallableQueryRowCount<T> call) {
    super(new FutureTask<>(call));
    this.call = call;
  }

  public FutureTask<Integer> getFutureTask() {
    return futureTask;
  }

  public Transaction getTransaction() {
    return call.transaction;
  }

  public Query<T> getQuery() {
    return call.query;
  }

  public boolean cancel(boolean mayInterruptIfRunning) {
    call.query.cancel();
    return super.cancel(mayInterruptIfRunning);
  }


}
