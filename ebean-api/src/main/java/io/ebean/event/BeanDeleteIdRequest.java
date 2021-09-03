package io.ebean.event;

import io.ebean.Database;
import io.ebean.EbeanServer;
import io.ebean.Transaction;

/**
 * A request to delete a bean by Id value.
 */
public interface BeanDeleteIdRequest {

  /**
   * Deprecated migrate to database().
   */
  @Deprecated
  EbeanServer getEbeanServer();

  /**
   * Deprecated migrate to database().
   */
  @Deprecated
  default Database getDatabase() {
    return getEbeanServer();
  }

  /**
   * Return the DB processing the request.
   */
  default Database database() {
    return getEbeanServer();
  }

  /**
   * Return the Transaction associated with this request.
   */
  Transaction transaction();

  /**
   * Deprecated migrate to transaction().
   */
  @Deprecated
  default Transaction getTransaction() {
    return transaction();
  }

  /**
   * Returns the bean type of the bean being deleted.
   */
  Class<?> beanType();

  /**
   * Deprecated migrate to beanType().
   */
  @Deprecated
  default Class<?> getBeanType() {
    return beanType();
  }

  /**
   * Returns the Id value of the bean being deleted.
   */
  Object id();

  /**
   * Deprecated migrate to id().
   */
  @Deprecated
  default Object getId() {
    return id();
  }
}
