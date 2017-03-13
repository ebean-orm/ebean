package io.ebean;

import javax.persistence.PersistenceException;

/**
 * Thrown when a foreign key constraint is enforced.
 */
public class DataIntegrityException extends PersistenceException {

  /**
   * Create with a message and cause.
   */
  public DataIntegrityException(String message, Throwable cause) {
    super(message, cause);
  }
}
