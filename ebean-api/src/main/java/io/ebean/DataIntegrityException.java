package io.ebean;

import javax.persistence.PersistenceException;

/**
 * Thrown when a foreign key constraint is enforced or a field is too large.
 */
public class DataIntegrityException extends PersistenceException {
  private static final long serialVersionUID = -6740171949170180970L;

  /**
   * Create with a message and cause.
   */
  public DataIntegrityException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Create with message only.
   */
  public DataIntegrityException(String message) {
    super(message);
  }
}
