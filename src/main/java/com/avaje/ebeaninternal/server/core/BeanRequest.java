package com.avaje.ebeaninternal.server.core;

import java.sql.Connection;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiTransaction;

/**
 * Base class for find and persist requests.
 */
public abstract class BeanRequest {

	/**
	 * The server processing the request.
	 */
	final SpiEbeanServer ebeanServer;

	final String serverName;

	/**
	 * The transaction this is part of.
	 */
	SpiTransaction transaction;

	boolean createdTransaction;

	boolean readOnly;

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
				// commented out for performance reasons...
				// TODO: review performance of trans.setReadOnly(true)
				//if (readOnlyTransaction) {
				//	readOnly = true;
				//	transaction.setReadOnly(true);
				//}
				createdTransaction = true;
			}
		}
	}

	/**
	 * Commit this transaction if it was created for this request.
	 */
	public void commitTransIfRequired() {
		if (createdTransaction) {
			if (readOnly) {
				transaction.rollback();
			} else {
				transaction.commit();
			}
		}
	}

	/**
	 * Rollback the transaction if it was created for this request.
	 */
	public void rollbackTransIfRequired() {
		if (createdTransaction) {
			transaction.rollback();
		}
	}

	/**
	 * Return the server processing the request. Made available for
	 * BeanController and BeanFinder.
	 */
	public EbeanServer getEbeanServer() {
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
