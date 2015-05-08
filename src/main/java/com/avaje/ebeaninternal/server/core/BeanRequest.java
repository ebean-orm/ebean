package com.avaje.ebeaninternal.server.core;

import java.sql.Connection;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiTransaction;

/**
 * Base class for find and persist requests.
 */
public abstract class BeanRequest {

  private static final Logger log = LoggerFactory.getLogger(BeanRequest.class);
  
	/**
	 * The server processing the request.
	 */
	protected final SpiEbeanServer ebeanServer;

	protected final String serverName;

	/**
	 * The transaction this is part of.
	 */
	protected SpiTransaction transaction;

	protected boolean createdTransaction;

	public BeanRequest(SpiEbeanServer ebeanServer, SpiTransaction t) {
		this.ebeanServer = ebeanServer;
		this.serverName = ebeanServer.getName();
		this.transaction = t;
	}

	/**
	 * initialise an implicit transaction if one is not currently supplied.
	 * <p>
	 * A transaction may have been passed in or active in the thread local. If
	 * not then create one implicitly to handle the request.
	 * </p>
	 */
	public abstract void initTransIfRequired();

	/**
	 * A helper method for creating an implicit transaction is it is required.
	 * <p>
	 * A transaction may have been passed in or active in the thread local. If
	 * not then create one implicitly to handle the request.
	 * </p>
	 */
	public void createImplicitTransIfRequired(boolean readOnlyTransaction) {
		if (transaction == null) {
			transaction = ebeanServer.getCurrentServerTransaction();
			if (transaction == null || !transaction.isActive()) {
				// create an implicit transaction to execute this query
				transaction = ebeanServer.createServerTransaction(false, -1);
				createdTransaction = true;
			}
		}
	}

  /**
   * Commit this transaction if it was created for this request.
   */
  public void commitTransIfRequired() {
    if (createdTransaction) {
      transaction.commit();
    }
  }

	/**
	 * Rollback the transaction if it was created for this request.
	 */
	public void rollbackTransIfRequired() {
		if (createdTransaction) {
		  try {
		    transaction.rollback();
		  } catch (PersistenceException e) {
		    // Just log this and carry on. A previous exception has been
		    // thrown and if this rollback throws exception it likely means
		    // that the connection is broken (and the datasource and db will cleanup)
		    log.error("Error trying to rollack a transaction (after a prior exception thrown)", e);
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
	 * Returns the connection from the Transaction.
	 */
	public Connection getConnection() {
		return transaction.getInternalConnection();
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
}
