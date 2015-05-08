package com.avaje.ebean.bean;

/**
 * Loads a entity bean.
 * <p>
 * Typically invokes lazy loading for a single or batch of entity beans.
 * </p>
 */
public interface BeanLoader {

  /**
   * Return the name of the associated EbeanServer.
   */
  String getName();

  /**
   * Invoke the lazy loading for this bean.
   */
  void loadBean(EntityBeanIntercept ebi);

}
