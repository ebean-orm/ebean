package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiQuery;

public class OrmQueryRequestTestHelper {

  static DefaultServer defaultServer = (DefaultServer) Ebean.getDefaultServer();

  /**
   * Create and return a OrmQueryRequest for the given query.
   */
  public static <T> OrmQueryRequest<T> queryRequest(Query<T> query) {
    return (OrmQueryRequest<T>) defaultServer.createQueryRequest(SpiQuery.Type.LIST, query, null);
  }

}
