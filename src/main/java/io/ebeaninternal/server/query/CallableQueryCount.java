package io.ebeaninternal.server.query;

import io.ebean.Transaction;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

import java.util.concurrent.Callable;

/**
 * Represent the findCount query as a Callable.
 *
 * @param <T> the entity bean type
 */
public class CallableQueryCount<T> extends CallableQuery<T> implements Callable<Integer> {

  /**
   * Note that the transaction passed in is always a new transaction solely to
   * find the row count so it must be cleaned up by this CallableQueryRowCount.
   */
  public CallableQueryCount(SpiEbeanServer server, SpiQuery<T> query, Transaction t) {
    super(server, query, t);
  }

  /**
   * Execute the query returning the row count.
   */
  @Override
  public Integer call() throws Exception {
    try {
      return server.findCountWithCopy(query, transaction);
    } finally {
      // cleanup the underlying connection
      transaction.end();
    }
  }

}
