package com.avaje.ebeaninternal.server.query;

import java.util.concurrent.Callable;

import com.avaje.ebean.Transaction;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;

/**
 * Represent the findRowCount query as a Callable.
 * 
 * @param <T>
 *          the entity bean type
 */
public class CallableQueryRowCount<T> extends CallableQuery<T> implements Callable<Integer> {

  /**
   * Note that the transaction passed in is always a new transaction solely to
   * find the row count so it must be cleaned up by this CallableQueryRowCount.
   */
  public CallableQueryRowCount(SpiEbeanServer server, SpiQuery<T> query, Transaction t) {
    super(server, query, t);
  }

  /**
   * Execute the query returning the row count.
   */
  public Integer call() throws Exception {
    try {
      return server.findRowCountWithCopy(query, transaction);
    } finally {
      // cleanup the underlying connection
      transaction.end();
    }
  }

}
