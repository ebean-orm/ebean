package io.ebean.spring.txn;

import io.avaje.applog.AppLog;
import io.ebean.TxScope;
import io.ebean.config.ExternalTransactionManager;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.transaction.TransactionManager;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.util.List;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * A Spring-aware {@link ExternalTransactionManager}.
 * <p>
 * Will look for Spring transactions and use them if they exist.
 *
 * @author E Mc Greal
 * @since 18.05.2009
 */
public final class SpringJdbcTransactionManager implements ExternalTransactionManager {

  private static final System.Logger log = AppLog.getLogger(SpringJdbcTransactionManager.class);

  private DataSource dataSource;
  private TransactionManager transactionManager;

  /**
   * Instantiates a new spring aware transaction scope manager.
   */
  public SpringJdbcTransactionManager() {
  }

  /**
   * Initialise this with the Ebean internal transaction manager.
   */
  @Override
  public void setTransactionManager(Object txnMgr) {
    // RB: At this stage not exposing TransactionManager to
    // the public API and hence the Object type and casting here
    this.transactionManager = (TransactionManager) txnMgr;
    this.dataSource = transactionManager.dataSource();
  }

  /**
   * Looks for a current Spring managed transaction and wraps/returns that as a Ebean transaction.
   * <p>
   * Returns null if there is no current spring transaction (lazy loading outside a spring txn).
   */
  @Override
  public Object getCurrentTransaction() {
    // Get the current Spring ConnectionHolder associated to the current spring managed transaction
    ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
    if (holder == null || !holder.isSynchronizedWithTransaction()) {
      // no current Spring transaction
      SpiTransaction currentEbeanTransaction = transactionManager.inScope();
      if (currentEbeanTransaction == null || !currentEbeanTransaction.isActive()) {
        return null;
      } else {
        return currentEbeanTransaction;
      }
    }

    SpringTxnListener springTxnLister = listener();
    if (springTxnLister != null) {
      // we have already seen this transaction
      return springTxnLister.transaction();
    } else {
      // This is a new spring transaction that we have not seen before.
      // "wrap" it in a SpringJdbcTransaction for use with Ebean
      SpringJdbcTransaction newTrans = new SpringJdbcTransaction(holder, transactionManager);

      // Create and register a Spring TransactionSynchronization for this transaction
      springTxnLister = createListener(newTrans);
      TransactionSynchronizationManager.registerSynchronization(springTxnLister);
      return transactionManager.externalBeginTransaction(newTrans, TxScope.required());
    }
  }

  /**
   * Search for our specific transaction listener.
   * <p>
   * If it exists then we have already seen and "wrapped" this transaction.
   */
  private SpringTxnListener listener() {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
      if (synchronizations != null) {
        // search for our specific listener
        for (TransactionSynchronization synchronization : synchronizations) {
          if (synchronization instanceof SpringTxnListener) {
            return (SpringTxnListener) synchronization;
          }
        }
      }
    }
    return null;
  }

  /**
   * Create a listener to enable Ebean to be notified when transactions commit and rollback.
   */
  private SpringTxnListener createListener(SpringJdbcTransaction transaction) {
    return new SpringTxnListener(transactionManager, transaction);
  }

  /**
   * A Spring TransactionSynchronization that we register with Spring to get notified when
   * a Spring managed transaction has been committed or rolled back.
   * <p>
   * When Ebean is notified (of the commit/rollback) it can then manage its cache, notify
   * BeanPersistListeners etc.
   */
  private static class SpringTxnListener extends TransactionSynchronizationAdapter {

    private final TransactionManager transactionManager;

    private final SpringJdbcTransaction transaction;

    private SpringTxnListener(TransactionManager transactionManager, SpringJdbcTransaction t) {
      this.transactionManager = transactionManager;
      this.transaction = t;
    }

    private SpringJdbcTransaction transaction() {
      return transaction;
    }

    @Override
    public void flush() {
      transaction.flush();
    }

    @Override
    public void beforeCommit(boolean readOnly) {
      if (!readOnly) {
        transaction.preCommit();
      }
    }

    @Override
    public void afterCompletion(int status) {
      switch (status) {
        case STATUS_COMMITTED:
          log.log(DEBUG, "Spring Txn [{0}] committed", transaction.getId());
          transaction.postCommit();
          break;

        case STATUS_ROLLED_BACK:
          log.log(DEBUG, "Spring Txn [{0}] rollback", transaction.getId());
          transaction.postRollback(null);
          break;

        default:
          // this should never happen
          throw new PersistenceException("Invalid status " + status);
      }

      // Remove this transaction object as it is completed
      transactionManager.externalRemoveTransaction();
    }
  }
}
