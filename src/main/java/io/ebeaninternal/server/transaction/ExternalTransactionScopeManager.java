package io.ebeaninternal.server.transaction;

import io.ebean.config.ExternalTransactionManager;
import io.ebeaninternal.api.SpiTransaction;

/**
 * A TransactionScopeManager aware of external transaction managers.
 */
public class ExternalTransactionScopeManager extends DefaultTransactionScopeManager {

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
  public SpiTransaction getActive() {
    return (SpiTransaction) externalManager.getCurrentTransaction();
  }

}
