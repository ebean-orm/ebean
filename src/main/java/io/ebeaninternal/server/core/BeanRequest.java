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

  /**
   * The server processing the request.
   */
  protected final SpiEbeanServer ebeanServer;

  /**
   * The transaction this is part of.
   */
  protected SpiTransaction transaction;

  protected boolean createdTransaction;

  public BeanRequest(SpiEbeanServer ebeanServer, SpiTransaction t) {
    this.ebeanServer = ebeanServer;
    this.transaction = t;
  }

  /**
   * A helper method for creating an implicit transaction is it is required.
   * <p>
   * A transaction may have been passed in or active in the thread local. If
   * not then create one implicitly to handle the request.
   * </p>
   *
   * @return True if a transaction was set (from current or created).
   */
  public boolean createImplicitTransIfRequired() {
    if (transaction != null) {
      return false;
    }
    transaction = ebeanServer.currentServerTransaction();
    if (transaction == null || !transaction.isActive()) {
      // create an implicit transaction to execute this query
      transaction = ebeanServer.beginServerTransaction();
      createdTransaction = true;
    }
    return true;
  }

  /**
   * Commit this transaction if it was created for this request.
   */
  public void commitTransIfRequired() {
    if (createdTransaction) {
      ebeanServer.commitTransaction();
    }
  }

  /**
   * Rollback the transaction if it was created for this request.
   */
  public void rollbackTransIfRequired() {
    if (createdTransaction) {
      try {
        ebeanServer.endTransaction();
      } catch (Exception e) {
        // Just log this and carry on. A previous exception has been
        // thrown and if this rollback throws exception it likely means
        // that the connection is broken (and the dataSource and db will cleanup)
        log.error("Error trying to rollback a transaction (after a prior exception thrown)", e);
      }
    }
  }

  /**
   * Return the server processing the request. Made available for
   * BeanController and BeanFinder.
   */
  public EbeanServer getEbeanServer() {
    return ebeanServer;
  }

  public SpiEbeanServer getServer() {
    return ebeanServer;
  }

  /**
   * Return the Transaction associated with this request.
   */
  public SpiTransaction getTransaction() {
    return transaction;
  }

  /**
   * Set the transaction to use for this request.
   */
  public void setTransaction(SpiTransaction transaction) {
    this.transaction = transaction;
  }

  /**
   * Return true if SQL should be logged for this transaction.
   */
  public boolean isLogSql() {
    return transaction.isLogSql();
  }

  /**
   * Return true if SUMMARY information should be logged for this transaction.
   */
  public boolean isLogSummary() {
    return transaction.isLogSummary();
  }

  /**
   * Return the DataTimeZone to use.
   */
  public DataTimeZone getDataTimeZone() {
    return ebeanServer.getDataTimeZone();
  }
}
