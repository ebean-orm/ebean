package io.ebeaninternal.api;

/**
 * Per Transaction logging of SQL, TXN and Summary messages.
 */
public interface SpiTxnLogger {

  String id();

  /**
   * Is debug logging enabled.
   */
  boolean isLogSql();

  /**
   * Is summary logging enabled.
   */
  boolean isLogSummary();

  /**
   * Log a SQL message.
   */
  void sql(String[] msg);

  /**
   * Log a Summary message.
   */
  void sum(String[] msg);

  /**
   * Log a Transaction message.
   */
  void txn(String[] args);

  /**
   * Transaction Committed.
   */
  void notifyCommit();

  /**
   * Query only transaction completed.
   */
  void notifyQueryOnly();

  /**
   * Transaction Rolled back.
   */
  void notifyRollback(Throwable cause);
}
