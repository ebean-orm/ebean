package io.ebeaninternal.server.properties;

import io.ebean.bean.EntityBean;

/**
 * The getter implementation for a given bean property.
 */
public interface BeanPropertyGetter {

  /**
   * Return the value of a given bean property.
   */
  Object get(EntityBean bean);

  Object getIntercept(EntityBean bean);

}
