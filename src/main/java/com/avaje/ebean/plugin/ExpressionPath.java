package com.avaje.ebean.plugin;

/**
 * A dot notation expression path.
 */
public interface ExpressionPath {

  /**
   * Return true if there is a property on the path that is a many property.
   */
  boolean containsMany();

  /**
   * Set a value to the bean for this expression path.
   *
   * @param bean  the bean to set the value on
   * @param value the value to set
   */
  void set(Object bean, Object value);
}
