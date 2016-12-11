package io.ebeaninternal.server.properties;

import io.ebean.bean.EntityBean;

/**
 * The setter for a given bean property.
 */
public interface BeanPropertySetter {

  /**
   * Set the property value of a bean.
   */
  void set(EntityBean bean, Object value);

  /**
   * Set the property value of a bean with interception checks.
   * <p>
   * This could invoke lazy loading and or oldValues creation.
   * </p>
   */
  void setIntercept(EntityBean bean, Object value);

}
