package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Represent the findList query as a Callable.
 */
public final class CallableQueryList<T> extends CallableQuery<T> implements Callable<List<T>> {

  private final boolean createdTransaction;

  public CallableQueryList(SpiEbeanServer server, SpiQuery<T> query, boolean createdTransaction) {
    super(server, query);
    this.createdTransaction = createdTransaction;
  }

  /**
   * Execute the query returning the resulting List.
   */
  @Override
  public List<T> call() {
    try {
      return server.findList(query);
    } finally {
      if (createdTransaction) {
        transaction.end();
      }
    }
  }

}
