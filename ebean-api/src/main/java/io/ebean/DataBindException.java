package io.ebean;

import javax.persistence.PersistenceException;

/**
 * Thrown when a bind validator detects a data bind error.
 */
public class DataBindException extends PersistenceException {
  private static final long serialVersionUID = -1755215106960660645L;

  /**
   * Create with a message.
   */
  public DataBindException(String message) {
    super(message);
  }

  /**
   * Create with a message and cause.
   */
  public DataBindException(String message, Throwable cause) {
    super(message, cause);
  }
}
