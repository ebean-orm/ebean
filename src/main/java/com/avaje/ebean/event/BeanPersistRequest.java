package com.avaje.ebean.event;

import java.util.Set;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;

/**
 * Holds the information available for a bean persist (insert, update or
 * delete).
 * <p>
 * This is made available for the BeanPersistControllers.
 * </p>
 */
public interface BeanPersistRequest<T> {

  /**
   * Return the server processing the request.
   */
  public EbeanServer getEbeanServer();

  /**
   * Return the Transaction associated with this request.
   */
  public Transaction getTransaction();

  /**
   * For an update or delete of a partially populated bean this is the set of
   * loaded properties and otherwise returns null.
   */
  public Set<String> getLoadedProperties();

  /**
   * For an update this is the set of properties that where updated.
   */
  public Set<String> getUpdatedProperties();

  /**
   * Returns the bean being inserted updated or deleted.
   */
  public T getBean();

  /**
   * Returns a bean containing the original values prior to the bean being
   * modified.
   * <p>
   * This is for updates only.
   * </p>
   */
  public T getOldValues();

}
