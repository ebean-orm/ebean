package io.ebean;

import javax.persistence.PessimisticLockException;

/**
 * Thrown when failing to acquire a pessimistic lock.
 * <p>
 * Typically when "select for update nowait" or "select for update" is being used and
 * the lock can not be obtained (as it is held by another transaction).
 * </p>
 */
public class AcquireLockException extends PessimisticLockException {
  private static final long serialVersionUID = -8585962352965876691L;

  /**
   * Create with a message and cause.
   */
  public AcquireLockException(String message, Throwable cause) {
    super(message, cause);
  }
}
