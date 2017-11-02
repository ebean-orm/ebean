package io.ebeaninternal.server.transaction;

import io.ebeaninternal.api.SpiProfileHandler;

/**
 * A do nothing SpiProfileHandler.
 */
public class NoopProfileHandler implements SpiProfileHandler {

  @Override
  public void collectTransactionProfile(TransactionProfile transactionProfile) {
    // do nothing
  }

  @Override
  public ProfileStream createProfileStream(int profileId) {
    // always return null
    return null;
  }
}
