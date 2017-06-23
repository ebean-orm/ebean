package io.ebean;

import javax.persistence.PersistenceException;

/**
 * Thrown when a foreign key constraint is enforced.
 */
public class DataIntegrityException extends PersistenceException {
  private static final long serialVersionUID = -6740171949170180970L;

  /**
   * Create with a message and cause.
   */
  public DataIntegrityException(String message, Throwable cause) {
    super(message, cause);
  }
}
