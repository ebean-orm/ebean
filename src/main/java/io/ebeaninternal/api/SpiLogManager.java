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
   * Return the SQL logger.
   */
  SpiLogger sql();

  /**
   * Return the TXN logger.
   */
  SpiLogger txn();

  /**
   * Return the Summary logger.
   */
  SpiLogger sum();

}
