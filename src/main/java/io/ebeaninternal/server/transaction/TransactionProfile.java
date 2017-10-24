package io.ebeaninternal.server.transaction;

/**
 * Profiling information for a single transaction that has completed.
 */
public class TransactionProfile {

  /**
   * The profileId of the transaction (On @Transactional explicitly or can be automatically set by enhancement).
   */
  private final int profileId;

  /**
   * The total execution time of the transaction (for filtering out small/short transactions).
   */
  private final long totalMicros;

  /**
   * The binary encoding of the transaction profiling events.
   */
  private final byte[] bytes;

  /**
   * Create with profileId, total micros and encoded profile data.
   */
  public TransactionProfile(int profileId, long totalMicros, byte[] bytes) {
    this.profileId = profileId;
    this.totalMicros = totalMicros;
    this.bytes = bytes;
  }

  /**
   * Return the transaction profileId.
   */
  public int getProfileId() {
    return profileId;
  }

  /**
   * Return the total transaction execution time in micros.
   */
  public long getTotalMicros() {
    return totalMicros;
  }

  /**
   * Return the profiling data in encoded form.
   */
  public byte[] getBytes() {
    return bytes;
  }
}
