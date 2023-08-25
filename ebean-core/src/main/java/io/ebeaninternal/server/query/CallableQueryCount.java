package io.ebeaninternal.server.query;

import io.ebean.Transaction;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

import java.util.concurrent.Callable;

/**
 * Represent the findCount query as a Callable.
 */
public final class CallableQueryCount<T> extends CallableQuery<T> implements Callable<Integer> {

  private final boolean createdTransaction;

  /**
   * Note that the transaction passed in is always a new transaction solely to
   * find the row count so it must be cleaned up by this CallableQueryRowCount.
   */
  public CallableQueryCount(SpiEbeanServer server, SpiQuery<T> query, Transaction t, boolean createdTransaction) {
    super(server, query, t);
    this.createdTransaction = createdTransaction;
  }

  /**
   * Execute the query returning the row count.
   */
  @Override
  public Integer call() {
    try {
      return server.findCountWithCopy(query, transaction);
    } finally {
      if (createdTransaction) {
        transaction.end();
      }
    }
  }

}
