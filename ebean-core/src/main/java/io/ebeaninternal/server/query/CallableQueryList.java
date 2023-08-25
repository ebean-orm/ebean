package io.ebeaninternal.server.query;

import io.ebean.Transaction;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Represent the findList query as a Callable.
 */
public final class CallableQueryList<T> extends CallableQuery<T> implements Callable<List<T>> {

  private final boolean createdTransaction;

  public CallableQueryList(SpiEbeanServer server, SpiQuery<T> query, Transaction t, boolean createdTransaction) {
    super(server, query, t);
    this.createdTransaction = createdTransaction;
  }

  /**
   * Execute the query returning the resulting List.
   */
  @Override
  public List<T> call() {
    try {
      return server.findList(query, transaction);
    } finally {
      if (createdTransaction) {
        transaction.end();
      }
    }
  }

}
