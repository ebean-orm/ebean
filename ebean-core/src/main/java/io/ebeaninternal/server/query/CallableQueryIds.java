package io.ebeaninternal.server.query;

import io.ebean.Transaction;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Represent the fetch Id's query as a Callable.
 */
public final class CallableQueryIds<T> extends CallableQuery<T> implements Callable<List<Object>> {

  private final boolean createdTransaction;

  public CallableQueryIds(SpiEbeanServer server, SpiQuery<T> query, Transaction t, boolean createdTransaction) {
    super(server, query, t);
    this.createdTransaction = createdTransaction;
  }

  /**
   * Execute the find Id's query returning the list of Id's.
   */
  @Override
  public List<Object> call() {
    // we have already made a copy of the query
    // this way the same query instance is available to the
    // QueryFutureIds (as so has access to the List before it is done)
    try {
      return server.findIdsWithCopy(query, transaction);
    } finally {
      if (createdTransaction) {
        transaction.end();
      }
    }
  }

}
