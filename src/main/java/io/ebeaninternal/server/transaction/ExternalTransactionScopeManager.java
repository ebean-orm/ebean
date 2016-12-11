package io.ebeaninternal.server.transaction;

import io.ebean.config.ExternalTransactionManager;
import io.ebeaninternal.api.SpiTransaction;

/**
 * A TransactionScopeManager aware of external transaction managers.
 */
public class ExternalTransactionScopeManager extends TransactionScopeManager {

  final ExternalTransactionManager externalManager;

  /**
   * Instantiates  transaction scope manager.
   *
   * @param transactionManager the transaction manager
   */
  public ExternalTransactionScopeManager(TransactionManager transactionManager, ExternalTransactionManager externalManager) {
    super(transactionManager);
    this.externalManager = externalManager;
  }

  public void commit() {
    DefaultTransactionThreadLocal.commit(serverName);
  }


  public void end() {
    DefaultTransactionThreadLocal.end(serverName);
  }

  public SpiTransaction get() {

    return (SpiTransaction) externalManager.getCurrentTransaction();
  }

  public void replace(SpiTransaction trans) {
    DefaultTransactionThreadLocal.replace(serverName, trans);
  }

  public void rollback() {
    DefaultTransactionThreadLocal.rollback(serverName);
  }

  public void set(SpiTransaction trans) {
    DefaultTransactionThreadLocal.set(serverName, trans);
  }
}
