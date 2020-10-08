package io.ebeaninternal.server.properties;

/**
 * Provides getter setter methods for beans.
 */
public interface BeanPropertyAccess {

  /**
   * Return the getter for a given bean property.
   */
  BeanPropertyGetter getGetter(int position);

  /**
   * Return the setter for a given bean property.
   */
  BeanPropertySetter getSetter(int position);
}
