package io.ebean.config;

/**
 * Throw when an processing an entity bean that is not bytecode enhanced.
 *
 * Refer: https://ebean-orm.github.io/docs/trouble-shooting#not-enhanced
 */
public class BeanNotEnhancedException extends IllegalStateException {

  public BeanNotEnhancedException(String msg) {
    super(msg);
  }
}
