package io.ebean.spring.txn;

import io.ebean.TxScope;
import io.ebean.config.ExternalTransactionManager;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.util.List;

/**
 * A Spring-aware {@link ExternalTransactionManager}.
 * <p>
 * Will look for Spring transactions and use them if they exist.
 * </p>
 *
 * @since 18.05.2009
 * @author E Mc Greal
 */
public class SpringJdbcTransactionManager implements ExternalTransactionManager {

  private static final Logger log = LoggerFactory.getLogger(SpringJdbcTransactionManager.class);

  /**
   * The data source.
   */
  private DataSource dataSource;

  /**
   * The Ebean transaction manager.
   */
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
    this.dataSource = transactionManager.getDataSource();
  }

  /**
   * Looks for a current Spring managed transaction and wraps/returns that as a Ebean transaction.
   * <p>
   * Returns null if there is no current spring transaction (lazy loading outside a spring txn etc).
   * </p>
   */
  @Override
  public Object getCurrentTransaction() {

    // Get the current Spring ConnectionHolder associated to the current spring managed transaction
    ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);

    if (holder == null || !holder.isSynchronizedWithTransaction()) {
      // no current Spring transaction
      SpiTransaction currentEbeanTransaction = transactionManager.getInScope();
      if (currentEbeanTransaction != null && currentEbeanTransaction.isActive()) { // this is unexpected
        log.warn("No current Spring transaction BUT using current Ebean one {}", currentEbeanTransaction.getId());
      } else {
        log.trace("No current Spring transaction");
      }
      return currentEbeanTransaction;
    }

    SpringTxnListener springTxnLister = getSpringTxnListener();
    if (springTxnLister != null) {
      // we have already seen this transaction
      return springTxnLister.getTransaction();
    } else {
      // This is a new spring transaction that we have not seen before.
      // "wrap" it in a SpringJdbcTransaction for use with Ebean
      SpringJdbcTransaction newTrans = new SpringJdbcTransaction(holder, transactionManager);

      // Create and register a Spring TransactionSynchronization for this transaction
      springTxnLister = createSpringTxnListener(newTrans);
      TransactionSynchronizationManager.registerSynchronization(springTxnLister);

      return transactionManager.externalBeginTransaction(newTrans, TxScope.required());
    }
  }

  /**
   * Search for our specific transaction listener.
   * <p>
   * If it exists then we have already seen and "wrapped" this transaction.
   * </p>
   */
  private SpringTxnListener getSpringTxnListener() {

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
   * Create a listener to register with Spring to enable Ebean to be
   * notified when transactions commit and rollback.
   * <p>
   * This is used by Ebean to notify it's appropriate listeners and maintain it's server
   * cache etc.
   * </p>
   */
  private SpringTxnListener createSpringTxnListener(SpringJdbcTransaction t) {
    return new SpringTxnListener(transactionManager, t);
  }

  /**
   * A Spring TransactionSynchronization that we register with Spring to get
   * notified when a Spring managed transaction has been committed or rolled
   * back.
   * <p>
   * When Ebean is notified (of the commit/rollback) it can then manage its
   * cache, notify BeanPersistListeners etc.
   * </p>
   */
  private static class SpringTxnListener extends TransactionSynchronizationAdapter {

    private final TransactionManager transactionManager;

    private final SpringJdbcTransaction transaction;

    private SpringTxnListener(TransactionManager transactionManager, SpringJdbcTransaction t) {
      this.transactionManager = transactionManager;
      this.transaction = t;
    }

    /**
     * Return the associated Ebean wrapped transaction.
     */
    SpringJdbcTransaction getTransaction() {
      return transaction;
    }

    @Override
    public void flush() {
      transaction.flushBatch();
    }

    @Override
    public void beforeCommit(boolean readOnly) {
      // Future note: for JPA2 locking we will
      // have beforeCommit events to fire
    }

    @Override
    public void afterCompletion(int status) {

      switch (status) {
        case STATUS_COMMITTED:
          log.debug("Spring Txn [{}] committed", transaction.getId());
          transactionManager.notifyOfCommit(transaction);
          break;

        case STATUS_ROLLED_BACK:
          log.debug("Spring Txn [{}] rollback", transaction.getId());
          transactionManager.notifyOfRollback(transaction, null);
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
