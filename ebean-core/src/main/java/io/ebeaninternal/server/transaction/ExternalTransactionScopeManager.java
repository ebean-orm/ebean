package io.ebeaninternal.server.transaction;

import io.ebean.config.ExternalTransactionManager;
import io.ebeaninternal.api.SpiTransaction;

/**
 * A TransactionScopeManager aware of external transaction managers.
 */
public final class ExternalTransactionScopeManager extends DefaultTransactionScopeManager {

  private final ExternalTransactionManager externalManager;

  /**
   * Instantiates  transaction scope manager.
   */
  public ExternalTransactionScopeManager(ExternalTransactionManager externalManager) {
    this.externalManager = externalManager;
  }

  @Override
  public void register(TransactionManager manager) {
    externalManager.setTransactionManager(manager);
  }

  @Override
  public SpiTransaction active() {
    return (SpiTransaction) externalManager.getCurrentTransaction();
  }

}
