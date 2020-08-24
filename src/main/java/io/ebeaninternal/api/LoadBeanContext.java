package io.ebeaninternal.api;


import io.ebean.bean.BeanCollection;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

/**
 * Controls the loading of ManyToOne and OneToOne relationships.
 */
public interface LoadBeanContext extends LoadSecondaryQuery {

  /**
   * Register a BeanCollection into the load context.
   */
  void register(BeanPropertyAssocMany<?> many, BeanCollection<?> collection);
}
