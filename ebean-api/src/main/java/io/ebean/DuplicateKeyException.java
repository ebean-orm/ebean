package io.ebean;

/**
 * Thrown when a duplicate is attempted on a unique constraint.
 * <p>
 * In terms of catching this exception with the view of continuing processing
 * using the same transaction look to use {@link Transaction#rollbackAndContinue()}.
 *
 * <pre>{@code
 *
 *   try (Transaction txn = database.beginTransaction()) {
 *
 *     try {
 *       ...
 *       database.save(bean);
 *       database.flush();
 *     } catch (DuplicateKeyException e) {
 *       // carry on processing using the transaction
 *       txn.rollbackAndContinue();
 *       ...
 *     }
 *
 *     txn.commit();
 *   }
 *
 * }</pre>
 */
public class DuplicateKeyException extends DataIntegrityException {
  private static final long serialVersionUID = -4771932723285724817L;

  /**
   * Create with a message and cause.
   */
  public DuplicateKeyException(String message, Throwable cause) {
    super(message, cause);
  }
}
