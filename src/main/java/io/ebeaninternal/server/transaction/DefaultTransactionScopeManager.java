package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;

/**
 * Manages the transaction scoping using a Ebean thread local.
 */
public class DefaultTransactionScopeManager extends TransactionScopeManager {


  public DefaultTransactionScopeManager(TransactionManager transactionManager) {
    super(transactionManager);
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
  public SpiTransaction get() {
    SpiTransaction t = DefaultTransactionThreadLocal.get(serverName);
    if (t == null || !t.isActive()) {
      return null;
    } else {
      return t;
    }
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
