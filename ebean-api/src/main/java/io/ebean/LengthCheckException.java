package io.ebean;

/**
 * Exception when the length check has an error.
 */
public class LengthCheckException extends DataIntegrityException {
  private static final long serialVersionUID = -4771932723285724817L;

  /**
   * Create with a message.
   */
  public LengthCheckException(String message) {
    super(message);
  }
}
