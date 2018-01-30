package io.ebeaninternal.server.transaction;

import io.ebean.config.ExternalTransactionManager;
import io.ebeaninternal.api.SpiTransaction;

/**
 * A TransactionScopeManager aware of external transaction managers.
 */
public class ExternalTransactionScopeManager extends TransactionScopeManager {

  private final ExternalTransactionManager externalManager;

  /**
   * Instantiates  transaction scope manager.
   */
  public ExternalTransactionScopeManager(String serverName, ExternalTransactionManager externalManager) {
    super(serverName);
    this.externalManager = externalManager;
  }

  @Override
  public void register(TransactionManager manager) {
    externalManager.setTransactionManager(manager);
  }

  @Override
  public void commit() {
    DefaultTransactionThreadLocal.commit(serverName);
  }


  @Override
  public void end() {
    DefaultTransactionThreadLocal.end(serverName);
  }

  @Override
  public SpiTransaction getMaybeInactive() {
    return get();
  }

  @Override
  public SpiTransaction get() {
    return (SpiTransaction) externalManager.getCurrentTransaction();
  }

  @Override
  public void replace(SpiTransaction trans) {
    DefaultTransactionThreadLocal.replace(serverName, trans);
  }

  @Override
  public void rollback() {
    DefaultTransactionThreadLocal.rollback(serverName);
  }

  @Override
  public void set(SpiTransaction trans) {
    DefaultTransactionThreadLocal.set(serverName, trans);
  }
}
