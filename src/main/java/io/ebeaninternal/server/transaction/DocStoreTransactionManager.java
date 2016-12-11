package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Transaction manager used for doc store only EbeanServer instance.
 * <p>
 * There is no underlying JDBC DataSource etc
 */
public class DocStoreTransactionManager extends TransactionManager {

  private final AtomicLong counter = new AtomicLong(1000L);

  /**
   * Create the TransactionManager
   */
  public DocStoreTransactionManager(TransactionManagerOptions options) {
    super(options);
  }

  @Override
  public SpiTransaction createTransaction(boolean explicit, int isolationLevel) {
    long id = counter.incrementAndGet();
    return createTransaction(explicit, null, id);
  }

  @Override
  public SpiTransaction createQueryTransaction(Object tenantId) {
    return new DocStoreOnlyTransaction("", false, this);
  }

  @Override
  protected SpiTransaction createTransaction(boolean explicit, Connection c, long id) {
    return new DocStoreOnlyTransaction(prefix + id, explicit, this);
  }
}
