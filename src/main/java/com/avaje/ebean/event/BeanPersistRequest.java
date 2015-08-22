package com.avaje.ebean.event;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.ValuePair;

import java.util.Map;
import java.util.Set;

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
   * <p>
   * Note that hasDirtyProperty() is a more efficient check than this method and
   * should be preferred if it satisfies the requirement.
   * </p>
   */
  Set<String> getUpdatedProperties();

  /**
   * Return true for an update request if at least one of dirty properties is contained
   * in the given set of property names.
   * <p>
   * This method will produce less GC compared with getUpdatedProperties() and should
   * be preferred if it satisfies the requirement.
   * </p>
   * <p>
   * Note that this method is used by the default ChangeLogFilter mechanism for when
   * the <code>@ChangeLog</code> updatesThatInclude attribute has been specified.
   * </p>
   *
   * @param propertyNames a set of property names which we are checking to see if at least
   *                      one of them is dirty.
   */
  boolean hasDirtyProperty(Set<String> propertyNames);

  /**
   * Returns the bean being inserted updated or deleted.
   */
  T getBean();

  /**
   * Returns a map of the properties that have changed and their new and old values.
   */
  Map<String, ValuePair> getUpdatedValues();

}
