package io.ebeaninternal.server.persist;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Context used for merge processing.
 */
class MergeContext {

  private final SpiEbeanServer server;

  private final SpiTransaction transaction;

  private final List<EntityBean> deleteBeans = new ArrayList<>();

  private boolean clientGeneratedIds;

  MergeContext(SpiEbeanServer server, SpiTransaction transaction, boolean clientGeneratedIds) {
    this.server = server;
    this.transaction = transaction;
    this.clientGeneratedIds = clientGeneratedIds;
  }

  public SpiEbeanServer getServer() {
    return server;
  }

  public SpiTransaction getTransaction() {
    return transaction;
  }

  /**
   * Add to the list of beans to delete.
   */
  void addDelete(EntityBean deleteBean) {
    deleteBeans.add(deleteBean);
  }

  /**
   * Return true if Ids must be checked against the DB.
   */
  boolean isClientGeneratedIds() {
    return clientGeneratedIds;
  }

  /**
   * Check if the Id / bean exists in the database.
   */
  boolean idExists(Class<?> beanType, Object beanId) {
    return server.exists(beanType, beanId, transaction);
  }

  /**
   * Return the list of beans to delete.
   */
  List<EntityBean> getDeletedBeans() {
    return deleteBeans;
  }
}
