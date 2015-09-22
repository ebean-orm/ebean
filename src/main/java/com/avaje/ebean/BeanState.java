package com.avaje.ebean;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;

/**
 * Provides access to the internal state of an entity bean.
 */
public interface BeanState {

  /**
   * Return true if this is a lazy loading reference bean.
   * <p>
   * If so the this bean only holds the Id property and will invoke lazy loading
   * if any other property is get or set.
   * </p>
   */
  boolean isReference();

  /**
   * Return true if the bean is new (and not yet saved).
   */
  boolean isNew();

  /**
   * Return true if the bean is new or dirty (and probably needs to be saved).
   */
  boolean isNewOrDirty();

  /**
   * Return true if the bean has been changed but not yet saved.
   */
  boolean isDirty();

  /**
   * This can be called with true to disable lazy loading on the bean.
   */
  void setDisableLazyLoad(boolean disableLazyLoading);

  /**
   * Set the loaded state of the property given it's name.
   *
   * <p>
   *   Typically this would be used to set the loaded state of a property
   *   to false to ensure that the specific property is excluded from a
   *   stateless update.
   * </p>
   *
   * <pre>{@code
   *
   *   // populate a bean via say JSON
   *   User user = ...;
   *
   *   // set loaded state on the email property to false so that
   *   // the email property is not included in a stateless update
   *   Ebean.getBeanState(user).setPropertyLoaded("email", false);
   *
   *   user.update();
   *
   * }</pre>
   *
   *
   * This will throw an IllegalArgumentException if the property is unknown.
   */
  void setPropertyLoaded(String propertyName, boolean loaded);

  /**
   * For partially populated beans returns the properties that are loaded on the
   * bean.
   * <p>
   * Accessing another property will cause lazy loading to occur.
   * </p>
   */
  Set<String> getLoadedProps();

  /**
   * Return the set of changed properties.
   */
  Set<String> getChangedProps();

  /**
   * Return a map of the updated properties and their new and old values.
   */
  Map<String,ValuePair> getDirtyValues();
  
  /**
   * Return true if the bean is readOnly.
   * <p>
   * If a setter is called on a readOnly bean it will throw an exception.
   * </p>
   */
  boolean isReadOnly();

  /**
   * Set the readOnly status for the bean.
   */
  void setReadOnly(boolean readOnly);

  /**
   * Add a propertyChangeListener.
   */
  void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Remove a propertyChangeListener.
   */
  void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * Advanced - Used to programmatically build a partially or fully loaded
   * entity bean. First create an entity bean via
   * {@link EbeanServer#createEntityBean(Class)}, then populate its properties
   * and then call this method specifying which properties where loaded or null
   * for a fully loaded entity bean.
   */
  void setLoaded();
}