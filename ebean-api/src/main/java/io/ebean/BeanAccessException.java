package io.ebean;

/**
 * Unsupported access of a property on an entity bean.
 * <p>
 * Attempted a lazy load operation on a bean that has disabled lazy loading
 * or attempt to mutate an unmodifiable bean.
 */
public class BeanAccessException extends UnsupportedOperationException {
  private static final long serialVersionUID = 1;

  /**
   * Create with no message.
   */
  public BeanAccessException() {
    super();
  }

  /**
   * Create with message.
   */
  public BeanAccessException(String message) {
    super(message);
  }
}
