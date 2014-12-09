package com.avaje.ebeaninternal.server.core;

import com.avaje.ebeaninternal.api.SpiTransaction;

/**
 * Used to temporarily wrap a thread local based transaction.
 * <p>
 * Additionally notes if the transaction was created by the request in which
 * case it needs to be commited after the request has been processed.
 * </p>
 */
final class TransWrapper {

	final SpiTransaction transaction;

	private final boolean wasCreated;

	/**
	 * Wrap the transaction indicating if it was just created.
	 */
	TransWrapper(SpiTransaction t, boolean created) {
		transaction = t;
		wasCreated = created;
	}

  void batchEscalateOnCollection() {
    transaction.checkBatchEscalationOnCollection();
  }

  void flushBatchOnCollection() {
    if (!wasCreated) {
      transaction.flushBatchOnCollection();
    }
  }

	void commitIfCreated() {
		if (wasCreated){
			transaction.commit();
		}
	}
	
	void rollbackIfCreated() {
		if (wasCreated){
			transaction.rollback();
		}
	}
	
	/**
	 * Return true if the transaction was just created. If true it should be
	 * committed after the request has been processed.
	 */
	boolean wasCreated() {
		return wasCreated;
	}

}