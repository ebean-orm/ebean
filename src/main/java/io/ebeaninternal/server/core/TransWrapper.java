package io.ebeaninternal.server.core;

import io.ebean.EbeanServer;
import io.ebeaninternal.api.SpiTransaction;

/**
 * Used to temporarily wrap a thread local based transaction.
 * <p>
 * Additionally notes if the transaction was created by the request in which
 * case it needs to be commited after the request has been processed.
 * </p>
 */
final class TransWrapper {

  final SpiTransaction transaction;

  private final EbeanServer server;

  private final boolean wasCreated;

  /**
   * Wrap the transaction indicating if it was just created.
   */
  TransWrapper(SpiTransaction t, boolean created, EbeanServer server) {
    this.transaction = t;
    this.server = server;
    this.wasCreated = created;
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
    if (wasCreated) {
      server.commitTransaction();
    }
  }

  void endIfCreated() {
    if (wasCreated) {
      server.endTransaction();
    }
  }

}
