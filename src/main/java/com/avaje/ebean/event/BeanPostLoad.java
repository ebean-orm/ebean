package com.avaje.ebean.event;

/**
 * Fired after a bean is fetched and loaded from the database.
 * <p>
 * Note that if want to totally change the finding, you need to use a BeanQueryAdapter
 * rather than using postLoad().
 * </p>
 */
public interface BeanPostLoad {

  /**
   * Return true if this BeanPostLoad instance should be registered
   * for post load on this entity type.
   */
  boolean isRegisterFor(Class<?> cls);

  /**
   * Called after every each bean is loaded from the database. You
   * can implement this to derive some information to set to the bean.
   */
  void postLoad(Object bean);

}
