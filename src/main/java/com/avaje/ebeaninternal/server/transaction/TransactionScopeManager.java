package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.SpiTransactionScopeManager;

/**
 * Manages the Transactions typically held in a ThreadLocal.
 */
public abstract class TransactionScopeManager implements SpiTransactionScopeManager {

	protected final TransactionManager transactionManager;
	
	protected final String serverName;

	public TransactionScopeManager(TransactionManager transactionManager){
		this.transactionManager = transactionManager;
		this.serverName = transactionManager.getServerName();
	}
	
	 /**
     * Return the current Transaction for this serverName and Thread.
     */
    public abstract SpiTransaction get();

    /**
     * Set a new Transaction for this serverName and Thread.
     */
    public abstract void set(SpiTransaction trans);
    
    /**
     * Commit the current transaction.
     */
    public abstract void commit();

    /**
     * Rollback the current transaction.
     */
    public abstract void rollback();


    /**
     * Rollback if required.
     */
    public abstract void end();

    /**
     * Replace the current transaction with this one.
     * <p>
     * Used for Background fetching and Nested transaction scopes.
     * </p>
     * <p>
     * Used for background fetching. Replaces the current transaction with a
     * 'dummy' transaction. The current transaction is given to the background
     * thread so it can continue the fetch.
     * </p>
     */
    public abstract void replace(SpiTransaction trans);
}
