package io.ebeaninternal.server.transaction;

import io.ebean.ProfileLocation;
import io.ebeaninternal.api.SpiProfileHandler;
import org.jspecify.annotations.Nullable;

/**
 * A do nothing SpiProfileHandler.
 */
public final class NoopProfileHandler implements SpiProfileHandler {

  @Override
  public void collectTransactionProfile(TransactionProfile transactionProfile) {
    // do nothing
  }

  @Override
  public ProfileStream createProfileStream(@Nullable ProfileLocation location) {
    // always return null
    return null;
  }
}
