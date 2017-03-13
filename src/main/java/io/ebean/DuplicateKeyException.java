package io.ebean;

/**
 * Thrown when a duplicate is attempted on a unique constraint.
 */
public class DuplicateKeyException extends DataIntegrityException {

  /**
   * Create with a message and cause.
   */
  public DuplicateKeyException(String message, Throwable cause) {
    super(message, cause);
  }
}
