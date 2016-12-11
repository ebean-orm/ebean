package io.ebean.event;

import io.ebean.EbeanServer;

/**
 * Fired after a bean is constructed, but not yet loaded from database.
 * <p>
 * Note: You MUST NOT set any default values, as in a following step,
 * properties will get unload. Use {@link BeanPostLoad} instead.
 * <p>
 * it's intended to do some dependency-injection here.
 * If you plan to use this feature you should use {@link EbeanServer#createEntityBean(Class)}
 * to create new beans.
 * </p>
 */
public interface BeanPostConstructListener {

  /**
   * Return true if this BeanPostConstructListener instance should be registered
   * for post construct on this entity type.
   */
  boolean isRegisterFor(Class<?> cls);

  /**
   * Called immediately after construction. Perform DI here.
   */
  void autowire(Object bean);

  /**
   * Called after every &#64;PostConstruct annotated method of the bean is executed
   */
  void postConstruct(Object bean);

  /**
   * Called after {@link EbeanServer#createEntityBean(Class)}. Only for new beans.
   * intended to set default values here.
   */
  void postCreate(Object bean);

}
