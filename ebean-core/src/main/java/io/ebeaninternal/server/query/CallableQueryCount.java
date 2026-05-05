package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

import java.util.concurrent.Callable;

/**
 * Represent the findCount query as a Callable.
 */
public final class CallableQueryCount<T> extends CallableQuery<T> implements Callable<Integer> {

  public CallableQueryCount(SpiEbeanServer server, SpiQuery<T> query) {
    super(server, query);
  }

  /**
   * Execute the query returning the row count.
   */
  @Override
  public Integer call() {
    return server.findCountWithCopy(query);
  }

}
