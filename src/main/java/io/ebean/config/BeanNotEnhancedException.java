package io.ebean.config;

/**
 * Throw when an processing an entity bean that is not bytecode enhanced.
 *
 * Refer: https://ebean.io/docs/trouble-shooting#not-enhanced
 */
public class BeanNotEnhancedException extends IllegalStateException {

  private static final long serialVersionUID = 3008101919425876799L;

  public BeanNotEnhancedException(String msg) {
    super(msg);
  }
}
