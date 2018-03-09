package io.ebean;

/**
 * Thrown when a duplicate is attempted on a unique constraint.
 * @deprecated use DataIntegrityException instead, as DuplicateKeyException is not always detectable.
 */
@Deprecated
public class DuplicateKeyException extends DataIntegrityException {
  private static final long serialVersionUID = -4771932723285724817L;

  /**
   * Create with a message and cause.
   */
  public DuplicateKeyException(String message, Throwable cause) {
    super(message, cause);
  }
}
