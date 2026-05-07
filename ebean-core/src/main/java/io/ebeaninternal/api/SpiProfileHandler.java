package io.ebeaninternal.api;

import io.ebean.ProfileLocation;
import io.ebeaninternal.server.transaction.ProfileStream;
import io.ebeaninternal.server.transaction.TransactionProfile;
import org.jspecify.annotations.Nullable;

/**
 * Handle the logging or processing of transaction profiling information that is collected.
 */
public interface SpiProfileHandler {

  /**
   * Create a profiling stream for this transaction, or return null to not profile this transaction.
   * <p>
   * The location is null for implicit read-only transactions (queries without an explicit transaction).
   * Handlers should return null when they choose not to profile a given transaction.
   * </p>
   *
   * @param location The profile location, or null for implicit transactions
   * @param label The transaction label
   */
  @Nullable ProfileStream createProfileStream(@Nullable ProfileLocation location, @Nullable String label);
}
