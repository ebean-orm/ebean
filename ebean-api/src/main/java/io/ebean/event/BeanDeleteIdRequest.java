package io.ebean.event;

import io.ebean.Database;
import io.ebean.Transaction;

/**
 * A request to delete a bean by Id value.
 */
public interface BeanDeleteIdRequest {

  /**
   * Return the DB processing the request.
   */
  Database database();

  /**
   * Return the Transaction associated with this request.
   */
  Transaction transaction();

  /**
   * Returns the bean type of the bean being deleted.
   */
  Class<?> beanType();

  /**
   * Returns the Id value of the bean being deleted.
   */
  Object id();

}
