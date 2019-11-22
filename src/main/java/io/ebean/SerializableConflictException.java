package io.ebean;

import javax.persistence.OptimisticLockException;

/**
 * Thrown at SERIALIZABLE isolation level for non-recoverable concurrent conflict.
 */
public class SerializableConflictException extends OptimisticLockException {

  private static final long serialVersionUID = 1L;

  /**
   * Create with a message and cause.
   */
  public SerializableConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
