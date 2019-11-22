package io.ebeaninternal.server.core;

import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiTransaction;

/**
 * Transaction started implicitly.
 * <p>
 * This transaction is automatically committed / ends at the end of the operation.
 * </p>
 */
final class ObtainedTransactionImplicit extends ObtainedTransaction {

  private final SpiEbeanServer server;

  /**
   * Wrap the transaction indicating if it was just created.
   */
  ObtainedTransactionImplicit(SpiTransaction t, SpiEbeanServer server) {
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

  @Override
  public void clearIfCreated() {
    server.clearServerTransaction();
  }

}
