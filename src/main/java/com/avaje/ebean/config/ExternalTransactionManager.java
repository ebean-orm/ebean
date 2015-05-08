package com.avaje.ebean.config;

/**
 * Provides awareness of externally managed transactions.
 */
public interface ExternalTransactionManager {

  /**
   * Set the transaction manager.
   * <p>
   * This will change when SPI is published but will do for now.
   * </p>
   */
  void setTransactionManager(Object transactionManager);

  /**
   * Return the current transaction or null if there is none.
   */
  Object getCurrentTransaction();

}
