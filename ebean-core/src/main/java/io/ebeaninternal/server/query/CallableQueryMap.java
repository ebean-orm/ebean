package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Represent the findMap query as a Callable.
 */
public final class CallableQueryMap<K, T> extends CallableQuery<T> implements Callable<Map<K, T>> {

  private final boolean createdTransaction;

  public CallableQueryMap(SpiEbeanServer server, SpiQuery<T> query, boolean createdTransaction) {
    super(server, query);
    this.createdTransaction = createdTransaction;
  }

  /**
   * Execute the query returning the resulting map.
   */
  @Override
  public Map<K, T> call() {
    try {
      return server.findMap(query);
    } finally {
      if (createdTransaction) {
        transaction.end();
      }
    }
  }

}
