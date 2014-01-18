package com.avaje.ebeaninternal.api;

import com.avaje.ebean.Transaction;

/**
 * Request for loading Associated One Beans.
 */
public abstract class LoadRequest {

	protected final boolean lazy;

	protected final Transaction transaction;

	public LoadRequest(Transaction transaction, boolean lazy) {

		this.transaction = transaction;
		this.lazy = lazy;
	}

	/**
	 * Return true if this is a lazy load and false if it is a secondary query.
	 */
	public boolean isLazy() {
		return lazy;
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
