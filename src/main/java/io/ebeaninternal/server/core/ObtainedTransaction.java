package io.ebeaninternal.server.core;

import io.ebeaninternal.api.SpiTransaction;

/**
 * Basic case where there is already a transaction.
 * <p>
 * Additionally notes if the transaction was created by the request in which
 * case it needs to be commited after the request has been processed.
 * </p>
 */
class ObtainedTransaction {

  final SpiTransaction transaction;

  /**
   * Wrap the transaction indicating if it was just created.
   */
  ObtainedTransaction(SpiTransaction t) {
    this.transaction = t;
  }

  /**
   * Return the transaction (that was implicitly created if necessary).
   */
  public SpiTransaction transaction() {
    return transaction;
  }

  /**
   * Commit if the transaction was created implicitly.
   */
  public void commitIfCreated() {
    // do nothing
  }

  /**
   * End the transaction if it was created implicitly.
   */
  public void endIfCreated() {
    // do nothing
  }

}
