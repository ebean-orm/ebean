package com.avaje.ebean;

import java.beans.PropertyChangeListener;
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
  public boolean isReference();

  /**
   * Return true if the bean is new (and not yet saved).
   */
  public boolean isNew();

  /**
   * Return true if the bean is new or dirty (and probably needs to be saved).
   */
  public boolean isNewOrDirty();

  /**
   * Return true if the bean has been changed but not yet saved.
   */
  public boolean isDirty();

  /**
   * For partially populated beans returns the properties that are loaded on the
   * bean.
   * <p>
   * Accessing another property will cause lazy loading to occur.
   * </p>
   */
  public Set<String> getLoadedProps();

  /**
   * Return the set of changed properties.
   */
  public Set<String> getChangedProps();

  /**
   * Return true if the bean is readOnly.
   * <p>
   * If a setter is called on a readOnly bean it will throw an exception.
   * </p>
   */
  public boolean isReadOnly();

  /**
   * Set the readOnly status for the bean.
   */
  public void setReadOnly(boolean readOnly);

  /**
   * Add a propertyChangeListener.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Remove a propertyChangeListener.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * Advanced - Used to programmatically build a reference object.
   * <p>
   * You can create a new EntityBean (
   * {@link EbeanServer#createEntityBean(Class)}, set its Id property and then
   * call this setReference() method.
   * </p>
   */
  public void setReference();

  /**
   * Advanced - Used to programmatically build a partially or fully loaded
   * entity bean. First create an entity bean via
   * {@link EbeanServer#createEntityBean(Class)}, then populate its properties
   * and then call this method specifying which properties where loaded or null
   * for a fully loaded entity bean.
   * 
   * @param loadedProperties
   *          the properties that where loaded or null for a fully loaded entity
   *          bean.
   */
  public void setLoaded(Set<String> loadedProperties);
}