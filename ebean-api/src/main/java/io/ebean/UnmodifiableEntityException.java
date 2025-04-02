package io.ebean;

/**
 * Attempted to modify a read only entity.
 */
public class UnmodifiableEntityException extends BeanAccessException {
  private static final long serialVersionUID = 1;

  /**
   * Create with no message.
   */
  public UnmodifiableEntityException() {
    super();
  }

  /**
   * Create with message.
   */
  public UnmodifiableEntityException(String message) {
    super(message);
  }
}
