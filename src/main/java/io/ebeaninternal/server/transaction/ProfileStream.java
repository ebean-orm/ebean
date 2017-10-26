package io.ebeaninternal.server.transaction;

/**
 * Collects the events of a transaction being profiled.
 */
public interface ProfileStream {

  /**
   * Return the offset in micros from the start of the transaction.
   */
  long offset();

  /**
   * Add a query event.
   */
  void addQueryEvent(String event, long offset, short beanTypeId, int beanCount, short queryId);

  /**
   * Add a persist event.
   */
  void addPersistEvent(String event, long offset, short beanTypeId, int beanCount);

  /**
   * Add the commit/rollback event.
   */
  void addEvent(String event, long startOffset);

  /**
   * Transaction completed collect the profiling information.
   */
  void end(TransactionManager manager);
}
