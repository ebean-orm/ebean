package io.ebeaninternal.api;

import javax.persistence.PersistenceException;

import io.ebean.CancelableQuery;

/**
 * Cancellable query, that has a delegate.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public interface SpiCancelableQuery extends CancelableQuery {

  /**
   * Checks if the query was cancelled.
   * @throws PersistenceException if query was cancelled.
   */
  void checkCancelled();

  /**
   * Set the underlying cancelable query (with the PreparedStatement).
   */
  void setCancelableQuery(CancelableQuery cancelableQuery);

}
