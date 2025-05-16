package io.ebean.event;

import io.ebean.Database;
import io.ebean.Transaction;

import java.util.List;

/**
 * A request to delete beans by Id values.
 */
public interface BeanDeleteIdsRequest {

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
  List<Object> ids();

}
