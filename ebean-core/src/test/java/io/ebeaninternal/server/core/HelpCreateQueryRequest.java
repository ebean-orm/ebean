package io.ebeaninternal.server.core;

import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebeaninternal.api.SpiQuery;

public class HelpCreateQueryRequest {

  public static <T> OrmQueryRequest<T> create(EbeanServer server, SpiQuery.Type type, Query<T> query, Transaction t) {

    DefaultServer defaultServer = (DefaultServer) server;
    return (OrmQueryRequest<T>)defaultServer.createQueryRequest(type, query, t);
  }
}
