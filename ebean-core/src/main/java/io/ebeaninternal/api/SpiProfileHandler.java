package io.ebeaninternal.api;

import io.ebean.ProfileLocation;
import io.ebeaninternal.server.transaction.ProfileStream;
import io.ebeaninternal.server.transaction.TransactionProfile;

/**
 * Handle the logging or processing of transaction profiling information that is collected.
 */
public interface SpiProfileHandler {

  /**
   * Process the collected transaction profiling information.
   * <p>
   * Note that profileId and totalMicros are part of the profilingData but passed separately as the handler
   * may filter what it processed based on this information (ignore short transactions, only process specific
   * profileId transactions etc).
   * </p>
   *
   * @param transactionProfile The transaction profile that has just been collected
   */
  void collectTransactionProfile(TransactionProfile transactionProfile);

  /**
   * Create a profiling stream if we are profiling this transaction.
   * Return null if we are not profiling this transaction.
   *
   * @param location The profile location
   */
  ProfileStream createProfileStream(ProfileLocation location);
}
