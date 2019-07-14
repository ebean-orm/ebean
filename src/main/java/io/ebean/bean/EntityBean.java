package io.ebean.bean;

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

  /**
   * Return all the property names in defined order.
   */
  default String[] _ebean_getPropertyNames() {
    throw new NotEnhancedException();
  }

  /**
   * Return the property name at the given position.
   */
  default String _ebean_getPropertyName(int pos) {
    throw new NotEnhancedException();
  }

  /**
   * Return the enhancement marker value.
   * <p>
   * This is the class name of the enhanced class and used to check that all
   * entity classes are enhanced (specifically not just a super class).
   * </p>
   */
  default String _ebean_getMarker() {
    throw new NotEnhancedException();
  }

  /**
   * Create and return a new entity bean instance.
   */
  default Object _ebean_newInstance() {
    throw new NotEnhancedException();
  }

  /**
   * Generated method that sets the loaded state on all the embedded beans on
   * this entity bean by using EntityBeanIntercept.setEmbeddedLoaded(Object o);
   */
  default void _ebean_setEmbeddedLoaded() {
    throw new NotEnhancedException();
  }

  /**
   * Return true if any embedded beans are new or dirty.
   */
  default boolean _ebean_isEmbeddedNewOrDirty() {
    throw new NotEnhancedException();
  }

  /**
   * Return the intercept for this object.
   */
  default EntityBeanIntercept _ebean_getIntercept() {
    throw new NotEnhancedException();
  }

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
  default EntityBeanIntercept _ebean_intercept() {
    throw new NotEnhancedException();
  }

  /**
   * Set the value of a field of an entity bean of this type.
   * <p>
   * Note that using this method bypasses any interception that otherwise occurs
   * on entity beans. That means lazy loading and oldValues creation.
   * </p>
   */
  default void _ebean_setField(int fieldIndex, Object value) {
    throw new NotEnhancedException();
  }

  /**
   * Set the field value with interception.
   */
  default void _ebean_setFieldIntercept(int fieldIndex, Object value) {
    throw new NotEnhancedException();
  }

  /**
   * Return the value of a field from an entity bean of this type.
   * <p>
   * Note that using this method bypasses any interception that otherwise occurs
   * on entity beans. That means lazy loading.
   * </p>
   */
  default Object _ebean_getField(int fieldIndex) {
    throw new NotEnhancedException();
  }

  /**
   * Return the field value with interception.
   */
  default Object _ebean_getFieldIntercept(int fieldIndex) {
    throw new NotEnhancedException();
  }

}
