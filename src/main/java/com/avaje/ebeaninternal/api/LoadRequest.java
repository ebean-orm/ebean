package com.avaje.ebeaninternal.api;

import com.avaje.ebean.Transaction;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;

/**
 * Request for loading Associated One Beans.
 */
public abstract class LoadRequest {

  protected final OrmQueryRequest<?> parentRequest;

  protected final Transaction transaction;

  protected final boolean lazy;

	public LoadRequest(OrmQueryRequest<?> parentRequest, boolean lazy) {

	  this.parentRequest = parentRequest;
		this.transaction = parentRequest == null ? null : parentRequest.getTransaction();
		this.lazy = lazy;
	}

  /**
   * Log the just executed secondary query with the 'root' query if 'logSecondaryQuery' is set to
   * true. This is for testing purposes to confirm the secondary query executes etc.
   */
  public void logSecondaryQuery(SpiQuery<?> query) {
    if (parentRequest != null && parentRequest.isLogSecondaryQuery()) {
      parentRequest.getQuery().logSecondaryQuery(query);
    }
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
