package com.avaje.ebean.event;

import java.util.Map;
import java.util.Set;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.ValuePair;

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
  EbeanServer getEbeanServer();

  /**
   * Return the Transaction associated with this request.
   */
  Transaction getTransaction();

  /**
   * For an update or delete of a partially populated bean this is the set of
   * loaded properties and otherwise returns null.
   */
  Set<String> getLoadedProperties();

  /**
   * For an update this is the set of properties that where updated.
   */
  Set<String> getUpdatedProperties();

  /**
   * Returns the bean being inserted updated or deleted.
   */
  T getBean();

  /**
   * Returns a map of the properties that have changed and their new and old values.
   */
  Map<String,ValuePair> getUpdatedValues();

}
