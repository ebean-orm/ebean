package io.ebeaninternal.server.core;

import io.ebean.DB;
import io.ebean.Query;
import io.ebeaninternal.api.SpiQuery;

public class OrmQueryRequestTestHelper {

  static DefaultServer defaultServer = (DefaultServer) DB.getDefault();

  /**
   * Create and return a OrmQueryRequest for the given query.
   */
  @SuppressWarnings("unchecked")
  public static <T> OrmQueryRequest<T> queryRequest(Query<T> query) {
    return (OrmQueryRequest<T>) defaultServer.createQueryRequest(SpiQuery.Type.LIST, (SpiQuery<? extends Object>) query);
  }

}
