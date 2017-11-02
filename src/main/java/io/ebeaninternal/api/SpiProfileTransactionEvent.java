package io.ebeaninternal.api;

/**
 * Event that adds to a profiling transaction.
 */
public interface SpiProfileTransactionEvent {

  /**
   * Add the event information to the profiling transaction.
   */
  void profile();
}
