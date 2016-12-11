package io.ebean.event;

import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.Transaction;

/**
 * Holds the information available for a bean query.
 */
public interface BeanQueryRequest<T> {

  /**
   * Return the server processing the request.
   */
  EbeanServer getEbeanServer();

  /**
   * Return the Transaction associated with this request.
   */
  Transaction getTransaction();

  /**
   * Returns the query.
   */
  Query<T> getQuery();

}
