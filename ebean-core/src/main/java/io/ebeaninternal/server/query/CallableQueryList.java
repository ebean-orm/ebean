package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Represent the findList query as a Callable.
 */
public final class CallableQueryList<T> extends CallableQuery<T> implements Callable<List<T>> {

  public CallableQueryList(SpiEbeanServer server, SpiQuery<T> query) {
    super(server, query);
  }

  @Override
  public List<T> call() {
    return server.findList(query);
  }

}
