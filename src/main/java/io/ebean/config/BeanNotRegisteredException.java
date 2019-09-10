package io.ebean.config;

/**
 * Throw when an processing thinks a bean is not registered.
 *
 * Refer: https://ebean.io/docs/trouble-shooting#not-registered
 */
public class BeanNotRegisteredException extends IllegalStateException {

  private static final long serialVersionUID = -1411298126011136552L;

  public BeanNotRegisteredException(String msg) {
    super(msg);
  }
}
