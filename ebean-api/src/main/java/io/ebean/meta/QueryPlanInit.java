package io.ebean.meta;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Initiate query plan collection for plans by their hash or all query plans.
 */
public class QueryPlanInit {

  private final Map<String,Long> hashes = new HashMap<>();

  private boolean all;

  private long defaultThresholdMicros;

  /**
   * Return true if this initiates bind collection on all query plans.
   */
  public boolean isAll() {
    return all;
  }

  /**
   * Set to true to initiate bind collection on all query plans.
   */
  public void setAll(boolean all) {
    this.all = all;
  }

  /**
   * Return the default query execution time threshold which must be exceeded to initiate
   * query plan collection.
   */
  public long thresholdMicros() {
    return defaultThresholdMicros;
  }

  /**
   * Set the default query execution time threshold which must be exceeded to initiate
   * query plan collection.
   */
  public void thresholdMicros(long thresholdMicros) {
    this.defaultThresholdMicros = thresholdMicros;
  }

  /**
   * Return true if the query plan should be initiated based on it's hash.
   */
  public boolean includeHash(String hash) {
    return all || hashes.containsKey(hash);
  }

  /**
   * Return the specific hashes that we want to collect query plans on.
   *
   * @param hash            The hash of the query plan.
   * @param thresholdMicros The threshold in micros to use.
   */
  public void add(String hash, long thresholdMicros) {
    requireNonNull(hash);
    if (!"all".equals(hash)) {
      hashes.put(hash, thresholdMicros);
    } else {
      all = true;
      if (thresholdMicros > 0) {
        defaultThresholdMicros = thresholdMicros;
      }
    }
  }

  /**
   * Remove a hash from this request.
   */
  public void remove(String hash) {
    hashes.remove(hash);
  }

  /**
   * Return the threshold in micros to use for the given hash.
   */
  public long thresholdMicros(String hash) {
    long threshold = hashes.get(hash);
    return threshold < 1 ? defaultThresholdMicros : threshold;
  }

  /**
   * Return true if there are no registered hashes and not collect <em>All</em> plans.
   */
  public boolean isEmpty() {
    return !all && hashes.isEmpty();
  }

  @Override
  public String toString() {
    return "QueryPlanInit{" +
      "all=" + all +
      ", hashes=" + hashes +
      ", thresholdMicros=" + defaultThresholdMicros +
      '}';
  }
}
