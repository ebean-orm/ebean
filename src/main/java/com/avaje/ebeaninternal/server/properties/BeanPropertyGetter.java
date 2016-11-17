package com.avaje.ebeaninternal.server.properties;

import com.avaje.ebean.bean.EntityBean;

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
