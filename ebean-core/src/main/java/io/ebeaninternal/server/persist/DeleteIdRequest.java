package io.ebeaninternal.server.persist;

import io.ebean.Database;
import io.ebean.Transaction;
import io.ebean.event.BeanDeleteIdsRequest;
import io.ebeaninternal.api.SpiEbeanServer;

import java.util.List;

final class DeleteIdsRequest implements BeanDeleteIdsRequest {

  private final SpiEbeanServer server;
  private final Transaction transaction;
  private final Class<?> beanType;
  private List<Object> ids;

  DeleteIdsRequest(SpiEbeanServer server, Transaction transaction, Class<?> beanType, List<Object> ids) {
    this.server = server;
    this.transaction = transaction;
    this.beanType = beanType;
    this.ids = ids;
  }


  @Override
  public Database database() {
    return server;
  }

  @Override
  public Transaction transaction() {
    return transaction;
  }

  @Override
  public Class<?> beanType() {
    return beanType;
  }

  @Override
  public List<Object> ids() {
    return ids;
  }
}
