package io.ebeaninternal.server.persist;

import io.ebean.EbeanServer;
import io.ebean.Transaction;
import io.ebean.event.BeanDeleteIdRequest;
import io.ebeaninternal.api.SpiEbeanServer;

class DeleteIdRequest implements BeanDeleteIdRequest {

  private final EbeanServer server;
  private final Transaction transaction;
  private Object id;

  DeleteIdRequest(SpiEbeanServer server, Transaction transaction, Object id) {
    this.server = server;
    this.transaction = transaction;
  }

  void setId(Object id) {
    this.id = id;
  }

  @Override
  public EbeanServer getEbeanServer() {
    return server;
  }

  @Override
  public Transaction getTransaction() {
    return transaction;
  }

  @Override
  public Object getId() {
    return id;
  }
}
