package io.ebeaninternal.api;

/**
 * Logger for SQL, TXN and Summary logging.
 * <p>
 * In general at runtime this uses SLF4J Logger but this abstraction allows us to capture
 * the logged SQL during testing such that we can assert against the executed sql if desired.
 * </p>
 */
public interface SpiLogger {

  /**
   * Is debug logging enabled.
   */
  boolean isDebug();

  /**
   * Log a debug level message.
   */
  void debug(String msg, Object... args);

}
