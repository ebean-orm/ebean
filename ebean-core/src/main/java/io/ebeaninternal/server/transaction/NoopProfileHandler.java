package io.ebeaninternal.server.transaction;

import io.ebean.ProfileLocation;
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
  public ProfileStream createProfileStream(ProfileLocation location) {
    // always return null
    return null;
  }
}
