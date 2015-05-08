package com.avaje.ebean.bean;

import java.beans.PropertyChangeListener;
import java.io.Serializable;

/**
 * Bean that is aware of EntityBeanIntercept.
 * <p>
 * This interface and implementation of these methods is added to Entity Beans
 * via instrumentation. These methods have a funny _ebean_ prefix to avoid any
 * clash with normal methods these beans would have. These methods are not for
 * general application consumption.
 * </p>
 */
public interface EntityBean extends Serializable {

  String[] _ebean_getPropertyNames();
  
  String _ebean_getPropertyName(int pos);
  
  /**
   * Return the enhancement marker value.
   * <p>
   * This is the class name of the enhanced class and used to check that all
   * entity classes are enhanced (specifically not just a super class).
   * </p>
   */
  String _ebean_getMarker();

  /**
   * Create and return a new entity bean instance.
   */
  Object _ebean_newInstance();

  /**
   * Add a PropertyChangeListener to this bean.
   */
  void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Remove a PropertyChangeListener from this bean.
   */
  void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * Generated method that sets the loaded state on all the embedded beans on
   * this entity bean by using EntityBeanIntercept.setEmbeddedLoaded(Object o);
   */
  void _ebean_setEmbeddedLoaded();

  /**
   * Return true if any embedded beans are new or dirty.
   */
  boolean _ebean_isEmbeddedNewOrDirty();

  /**
   * Return the intercept for this object.
   */
  EntityBeanIntercept _ebean_getIntercept();

  /**
   * Similar to _ebean_getIntercept() except it checks to see if the intercept
   * field is null and will create it if required.
   * <p>
   * This is really only required when transientInternalFields=true as an
   * enhancement option. In this case the intercept field is transient and will
   * be null after a bean has been deserialised.
   * </p>
   * <p>
   * This transientInternalFields=true option was to support some serialization
   * frameworks that can't take into account our ebean fields.
   * </p>
   */
  EntityBeanIntercept _ebean_intercept();

  /**
   * Create a copy of this entity bean.
   * <p>
   * This occurs when a bean is changed. The copy represents the bean as it was
   * initially (oldValues) before any changes where made. This is used for
   * optimistic concurrency control.
   * </p>
   */
  Object _ebean_createCopy();

  /**
   * Set the value of a field of an entity bean of this type.
   * <p>
   * Note that using this method bypasses any interception that otherwise occurs
   * on entity beans. That means lazy loading and oldValues creation.
   * </p>
   */
  void _ebean_setField(int fieldIndex, Object value);

  /**
   * Set the field value with interception.
   */
  void _ebean_setFieldIntercept(int fieldIndex, Object value);

  /**
   * Return the value of a field from an entity bean of this type.
   * <p>
   * Note that using this method bypasses any interception that otherwise occurs
   * on entity beans. That means lazy loading.
   * </p>
   */
  Object _ebean_getField(int fieldIndex);

  /**
   * Return the field value with interception.
   */
  Object _ebean_getFieldIntercept(int fieldIndex);

}
