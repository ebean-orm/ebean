package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiTransaction;

/**
 * Manages the transaction scoping using a Ebean thread local.
 */
public class DefaultTransactionScopeManager extends TransactionScopeManager {


  public DefaultTransactionScopeManager(String serverName) {
    super(serverName);
  }

  @Override
  public void register(TransactionManager manager) {
    // do nothing
  }

  @Override
  public SpiTransaction getInScope() {
    return DefaultTransactionThreadLocal.get(serverName);
  }

  @Override
  public SpiTransaction getActive() {
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
  public void set(SpiTransaction trans) {
    DefaultTransactionThreadLocal.set(serverName, trans);
  }


}
