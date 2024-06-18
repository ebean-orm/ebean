package io.ebeaninternal.server.transaction;

import io.ebean.config.ExternalTransactionManager;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.SpiTransaction;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.persistence.PersistenceException;
import javax.sql.DataSource;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.UserTransaction;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Hook into external JTA transaction manager.
 */
public final class JtaTransactionManager implements ExternalTransactionManager {

  private static final System.Logger log = CoreLog.internal;
  private static final String EBEAN_TXN_RESOURCE = "EBEAN_TXN_RESOURCE";

  private TransactionManager transactionManager;
  private TransactionScopeManager scope;

  /**
   * Instantiates a new JTA transaction manager.
   */
  public JtaTransactionManager() {
  }

  /**
   * Initialise this with the Ebean internal transaction manager.
   */
  @Override
  public void setTransactionManager(Object txnMgr) {
    // RB: At this stage not exposing TransactionManager to
    // the public API and hence the Object type and casting here
    this.transactionManager = (TransactionManager) txnMgr;
    this.scope = transactionManager.scope();
  }

  /**
   * Return the current dataSource taking into account multi-tenancy.
   */
  private DataSource dataSource() {
    return transactionManager.dataSource();
  }

  private TransactionSynchronizationRegistry registry() {
    try {
      InitialContext ctx = new InitialContext();
      return (TransactionSynchronizationRegistry) ctx.lookup("java:comp/TransactionSynchronizationRegistry");
    } catch (NamingException e) {
      throw new PersistenceException(e);
    }
  }

  private UserTransaction userTransaction() {
    try {
      InitialContext ctx = new InitialContext();
      return (UserTransaction) ctx.lookup("java:comp/UserTransaction");
    } catch (NamingException e) {
      // assuming CMT
      return new DummyUserTransaction();
    }
  }

  /**
   * Looks for a current JTA managed transaction and wraps/returns that as an Ebean transaction.
   * <p>
   * Returns null if there is no current spring transaction (lazy loading outside a spring txn etc).
   */
  @Override
  public Object getCurrentTransaction() {
    TransactionSynchronizationRegistry syncRegistry;
    try {
      syncRegistry = registry();
      SpiTransaction t = (SpiTransaction) syncRegistry.getResource(EBEAN_TXN_RESOURCE);
      if (t != null) {
        // we have already seen this transaction
        return t;
      }
    } catch (Exception e) {
      // deem that there is no current transaction
      return null;
    }

    // check current Ebean transaction
    SpiTransaction currentEbeanTransaction = scope.inScope();
    if (currentEbeanTransaction != null) {
      if (currentEbeanTransaction.isActive()) {
        // NOT expecting this so log WARNING
        log.log(WARNING, "JTA Transaction - no current txn BUT using current Ebean one {0}", currentEbeanTransaction.id());
        return currentEbeanTransaction;
      } else {
        log.log(WARNING, "JTA Transaction - no current txn, but found Ebean inactive transaction. Clearing.", currentEbeanTransaction.id());
        // thread local references no longer active transaction. This can happen if the JtaTxnListener#afterCompletion
        // was triggered by a different thread than the one which created the scope (ThreadLocal).
        scope.clearExternal();
      }
    }

    UserTransaction ut = userTransaction();
    if (ut == null) {
      // no current JTA transaction
      if (log.isLoggable(DEBUG)) {
        log.log(DEBUG, "JTA Transaction - no current txn");
      }
      return null;
    }

    // This is a transaction that Ebean has not seen before.

    // "wrap" it in a Ebean specific JtaTransaction
    JtaTransaction newTrans = new JtaTransaction( true, ut, dataSource(), transactionManager);

    // create and register transaction listener
    JtaTxnListener txnListener = createJtaTxnListener(newTrans);

    syncRegistry.putResource(EBEAN_TXN_RESOURCE, newTrans);
    syncRegistry.registerInterposedSynchronization(txnListener);

    // also put in Ebean ThreadLocal
    scope.set(newTrans);
    return newTrans;
  }

  /**
   * Create a listener to register with JTA to enable Ebean to be notified when transactions commit and rollback.
   * <p>
   * This is used by Ebean to notify its appropriate listeners and maintain its server cache etc.
   */
  private JtaTxnListener createJtaTxnListener(SpiTransaction t) {
    return new JtaTxnListener(transactionManager, t);
  }

  private static class DummyUserTransaction implements UserTransaction {

    @Override
    public void begin() {
    }

    @Override
    public void commit() throws SecurityException, IllegalStateException {
    }

    @Override
    public int getStatus() {
      return 0;
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException {
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException {
    }

    @Override
    public void setTransactionTimeout(int seconds) {
    }
  }

  /**
   * A JTA Transaction Synchronization that we register to get notified when a
   * managed transaction has been committed or rolled back.
   * <p>
   * When Ebean is notified (of the commit/rollback) it can then manage its
   * cache, notify BeanPersistListeners etc.
   */
  private static class JtaTxnListener implements Synchronization {

    private final TransactionManager transactionManager;

    private final SpiTransaction transaction;

    private JtaTxnListener(TransactionManager transactionManager, SpiTransaction t) {
      this.transactionManager = transactionManager;
      this.transaction = t;
    }

    @Override
    public void beforeCompletion() {
      transaction.preCommit();
    }

    @Override
    public void afterCompletion(int status) {
      switch (status) {
        case Status.STATUS_COMMITTED:
          log.log(DEBUG, "Jta Txn [{0}] committed", transaction.id());
          transaction.postCommit();
          // Remove this transaction object as it is completed
          transactionManager.scope().clearExternal();
          break;

        case Status.STATUS_ROLLEDBACK:
          log.log(DEBUG, "Jta Txn [{0}] rollback", transaction.id());
          transaction.postRollback(null);
          // Remove this transaction object as it is completed
          transaction.setActive(false);
          transactionManager.scope().clearExternal();
          break;

        default:
          log.log(DEBUG, "Jta Txn [{0}] status:{1}", transaction.id(), status);
      }

      // No matter the completion status of the transaction, we release the connection we got from the pool.
      JdbcClose.close(transaction.internalConnection());
    }
  }

}
