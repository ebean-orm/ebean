package io.ebeaninternal.api;


import io.ebean.bean.BeanCollection;

/**
 * Controls the loading of ManyToOne and OneToOne relationships.
 */
public interface LoadBeanContext extends LoadSecondaryQuery {

  /**
   * Register a BeanCollection into the load context.
   */
  void register(String manyProperty, BeanCollection<?> collection);
}
