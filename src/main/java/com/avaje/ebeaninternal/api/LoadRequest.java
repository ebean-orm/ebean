package com.avaje.ebeaninternal.api;

import com.avaje.ebean.Transaction;

/**
 * Request for loading Associated One Beans.
 */
public abstract class LoadRequest {

	protected final boolean lazy;

	protected final int batchSize;

	protected final Transaction transaction;

	public LoadRequest(Transaction transaction, int batchSize, boolean lazy) {

		this.transaction = transaction;
		this.batchSize = batchSize;
		this.lazy = lazy;
	}


	/**
	 * Return true if this is a lazy load and false if it is a secondary query.
	 */
	public boolean isLazy() {
		return lazy;
	}

	/**
	 * Return the requested batch size.
	 */
	public int getBatchSize() {
		return batchSize;
	}

	/**
	 * Return the transaction to use if this is a secondary query.
	 * <p>
	 * Lazy loading queries run in their own transaction.
	 * </p>
	 */
	public Transaction getTransaction() {
		return transaction;
	}

}
