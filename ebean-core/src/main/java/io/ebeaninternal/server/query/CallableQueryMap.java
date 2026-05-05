package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Represent the findMap query as a Callable.
 */
public final class CallableQueryMap<K, T> extends CallableQuery<T> implements Callable<Map<K, T>> {

  public CallableQueryMap(SpiEbeanServer server, SpiQuery<T> query) {
    super(server, query);
  }

  @Override
  public Map<K, T> call() {
    return server.findMap(query);
  }

}
