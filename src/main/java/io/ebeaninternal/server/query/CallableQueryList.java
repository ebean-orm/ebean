package io.ebeaninternal.server.query;

import io.ebean.Transaction;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Represent the findList query as a Callable.
 *
 * @param <T> the entity bean type
 */
public class CallableQueryList<T> extends CallableQuery<T> implements Callable<List<T>> {


  public CallableQueryList(SpiEbeanServer server, SpiQuery<T> query, Transaction t) {
    super(server, query, t);
  }

  /**
   * Execute the query returning the resulting List.
   */
  @Override
  public List<T> call() throws Exception {
    try {
      return server.findList(query, transaction);
    } finally {
      // cleanup the underlying connection
      transaction.end();
    }
  }


}
