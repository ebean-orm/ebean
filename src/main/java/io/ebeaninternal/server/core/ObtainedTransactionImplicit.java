package io.ebeaninternal.server.core;

import io.ebean.EbeanServer;
import io.ebeaninternal.api.SpiTransaction;

/**
 * Transaction started implicitly.
 * <p>
 * This transaction is automatically committed / ends at the end of the operation.
 * </p>
 */
final class ObtainedTransactionImplicit extends ObtainedTransaction {

  private final EbeanServer server;

  /**
   * Wrap the transaction indicating if it was just created.
   */
  ObtainedTransactionImplicit(SpiTransaction t, EbeanServer server) {
    super(t);
    this.server = server;
  }

  @Override
  public void commitIfCreated() {
    server.commitTransaction();
  }

  @Override
  public void endIfCreated() {
    server.endTransaction();
  }

}
