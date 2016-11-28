package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebeaninternal.api.SpiTransaction;

import java.sql.Connection;

/**
 * Transaction manager used for doc store only EbeanServer instance.
 * <p>
 * There is no underlying JDBC DataSource etc
 */
public class DocStoreTransactionManager extends TransactionManager {

  /**
   * Create the TransactionManager
   */
  public DocStoreTransactionManager(TransactionManagerOptions options) {
    super(options);
  }

  @Override
  public SpiTransaction createTransaction(boolean explicit, int isolationLevel) {
    long id = transactionCounter.incrementAndGet();
    return createTransaction(explicit, null, id);
  }

  @Override
  public SpiTransaction createQueryTransaction() {
    return new DocStoreOnlyTransaction("", false, this);
  }

  @Override
  protected SpiTransaction createTransaction(boolean explicit, Connection c, long id) {
    return new DocStoreOnlyTransaction(prefix + id, explicit, this);
  }
}
