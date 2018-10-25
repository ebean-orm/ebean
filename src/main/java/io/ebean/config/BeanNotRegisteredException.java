package io.ebean.config;

/**
 * Throw when an processing thinks a bean is not registered.
 *
 * Refer: https://ebean-orm.github.io/docs/trouble-shooting#not-registered
 */
public class BeanNotRegisteredException extends IllegalStateException {

  public BeanNotRegisteredException(String msg) {
    super(msg);
  }
}
