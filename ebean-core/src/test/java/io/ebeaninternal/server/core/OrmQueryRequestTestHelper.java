package io.ebeaninternal.server.core;

import io.ebean.Ebean;
import io.ebean.Query;
import io.ebeaninternal.api.SpiQuery;

public class OrmQueryRequestTestHelper {

  static DefaultServer defaultServer = (DefaultServer) Ebean.getDefaultServer();

  /**
   * Create and return a OrmQueryRequest for the given query.
   */
  public static <T> OrmQueryRequest<T> queryRequest(Query<T> query) {
    return (OrmQueryRequest<T>) defaultServer.createQueryRequest(SpiQuery.Type.LIST, query, null);
  }

}
