package io.ebeaninternal.api;

/**
 * API for Plugins to define the logger for io.ebean.SQL, io.ebean.TXN and io.ebean.SUM.
 */
public interface SpiLoggerFactory {

  /**
   * Create the logger given the name.
   */
  SpiLogger create(String name);
}
