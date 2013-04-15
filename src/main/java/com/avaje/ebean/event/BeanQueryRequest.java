package com.avaje.ebean.event;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;

/**
 * Holds the information available for a bean query.
 */
public interface BeanQueryRequest<T> {

  /**
   * Return the server processing the request.
   */
  public EbeanServer getEbeanServer();

  /**
   * Return the Transaction associated with this request.
   */
  public Transaction getTransaction();

  /**
   * Returns the query.
   */
  public Query<T> getQuery();

}
