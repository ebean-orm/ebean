package io.ebean.event;

import io.ebean.Database;
import io.ebean.EbeanServer;
import io.ebean.Transaction;

/**
 * A request to delete a bean by Id value.
 */
public interface BeanDeleteIdRequest {

  /**
   * Return the server processing the request.
   * @deprecated use {@link #getDatabase()}
   */
  EbeanServer getEbeanServer();

  /**
   * Return the DB processing the request.
   */
  default Database getDatabase() {
    return getEbeanServer();
  }
  
  /**
   * Return the Transaction associated with this request.
   */
  Transaction getTransaction();
  
  /**
   * Returns the bean type of the bean being deleted.
   */
  Class<?> getBeanType();

  /**
   * Returns the Id value of the bean being deleted.
   */
  Object getId();

}
