package io.ebeaninternal.api;

/**
 * Log manager for SQL, TXN and Summary logging.
 * <p>
 * In general at runtime this uses SLF4J Logger but this abstraction allows us to capture
 * the logged SQL during testing such that we can assert against the executed sql if desired.
 * </p>
 */
public interface SpiLogManager {

  /**
   * Enable bind logging.
   */
  boolean enableBindLog();

  /**
   * Logger used for general transactions.
   */
  SpiTxnLogger logger();

  /**
   * Logger used for read only transactions.
   */
  SpiTxnLogger readOnlyLogger();

  /**
   * Hmmmm, return the SQL logger for logging truncate statements.
   * <p>
   * Maybe we should get rid of this
   */
  SpiLogger sql();

}
