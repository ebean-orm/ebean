package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;

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
    return createTransaction(explicit, null);
  }

  @Override
  public SpiTransaction createReadOnlyTransaction(Object tenantId) {
    return new DocStoreOnlyTransaction("", false, this);
  }

  @Override
  protected SpiTransaction createTransaction(boolean explicit, Connection c) {
    return new DocStoreOnlyTransaction(nextTxnId(), explicit, this);
  }
}
