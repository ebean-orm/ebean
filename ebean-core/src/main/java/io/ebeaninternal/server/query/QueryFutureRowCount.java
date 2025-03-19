package io.ebeaninternal.server.query;

import io.ebean.FutureRowCount;
import io.ebean.Transaction;

import java.util.concurrent.FutureTask;

/**
 * Future implementation for the row count query.
 */
public final class QueryFutureRowCount<T> extends BaseFuture<Integer> implements FutureRowCount<T> {

  private final CallableQueryCount<T> call;

  public QueryFutureRowCount(CallableQueryCount<T> call) {
    super(new FutureTask<>(call));
    this.call = call;
  }

  public FutureTask<Integer> futureTask() {
    return futureTask;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    call.query.cancel();
    return super.cancel(mayInterruptIfRunning);
  }

}
