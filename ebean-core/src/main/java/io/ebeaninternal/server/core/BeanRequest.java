package io.ebeaninternal.server.core;

import io.ebean.EbeanServer;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for find and persist requests.
 */
public abstract class BeanRequest {

  static final Logger log = LoggerFactory.getLogger(BeanRequest.class);

  protected final SpiEbeanServer server;
  protected SpiTransaction transaction;
  protected boolean createdTransaction;

  public BeanRequest(SpiEbeanServer server, SpiTransaction transaction) {
    this.server = server;
    this.transaction = transaction;
  }

  /**
   * A helper method for creating an implicit transaction is it is required.
   * <p>
   * A transaction may have been passed in or active in the thread local. If
   * not then create one implicitly to handle the request.
   *
   * @return True if a transaction was set (from current or created).
   */
  public boolean createImplicitTransIfRequired() {
    if (transaction != null) {
      return false;
    }
    transaction = server.currentServerTransaction();
    if (transaction == null || !transaction.isActive()) {
      // create an implicit transaction to execute this query
      transaction = server.beginServerTransaction();
      createdTransaction = true;
    }
    return true;
  }

  /**
   * Commit this transaction if it was created for this request.
   */
  public void commitTransIfRequired() {
    if (createdTransaction) {
      server.commitTransaction();
    }
  }

  /**
   * Rollback the transaction if it was created for this request.
   */
  public void rollbackTransIfRequired() {
    if (createdTransaction) {
      try {
        server.endTransaction();
      } catch (Exception e) {
        // Just log this and carry on. A previous exception has been
        // thrown and if this rollback throws exception it likely means
        // that the connection is broken (and the dataSource and db will cleanup)
        log.error("Error trying to rollback a transaction (after a prior exception thrown)", e);
      }
    }
  }

  /**
   * Clear the transaction from the thread local for implicit transactions.
   */
  public void clearTransIfRequired() {
    if (createdTransaction) {
      server.clearServerTransaction();
    }
  }

  /**
   * Return the server processing the request. Made available for
   * BeanController and BeanFinder.
   */
  public EbeanServer getEbeanServer() {
    return server;
  }

  public SpiEbeanServer server() {
    return server;
  }

  /**
   * Return the Transaction associated with this request.
   */
  public SpiTransaction transaction() {
    return transaction;
  }

  /**
   * Set the transaction to use for this request.
   */
  public void transaction(SpiTransaction transaction) {
    this.transaction = transaction;
  }

  /**
   * Return true if SQL should be logged for this transaction.
   */
  public boolean logSql() {
    return transaction.isLogSql();
  }

  /**
   * Return true if SUMMARY information should be logged for this transaction.
   */
  public boolean logSummary() {
    return transaction.isLogSummary();
  }

  /**
   * Return the DataTimeZone to use.
   */
  public DataTimeZone dataTimeZone() {
    return server.dataTimeZone();
  }
}
