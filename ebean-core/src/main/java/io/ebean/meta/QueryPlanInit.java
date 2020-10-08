package io.ebean.meta;

import java.util.HashSet;
import java.util.Set;

/**
 * Initiate query plan collection for plans by their hash or all query plans.
 */
public class QueryPlanInit {

  private boolean all;

  private Set<String> hashes = new HashSet<>();

  private long thresholdMicros;

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
   * Return the query execution time threshold which must be exceeded to initiate
   * query plan collection.
   */
  public long getThresholdMicros() {
    return thresholdMicros;
  }

  /**
   * Set the query execution time threshold which must be exceeded to initiate
   * query plan collection.
   */
  public void setThresholdMicros(long thresholdMicros) {
    this.thresholdMicros = thresholdMicros;
  }

  /**
   * Return true if the query plan should be initiated based on it's hash.
   */
  public boolean includeHash(String hash) {
    return all || hashes.contains(hash);
  }

  /**
   * Return the specific hashes that we want to collect query plans on.
   */
  public Set<String> getHashes() {
    return hashes;
  }

  /**
   * Set the specific hashes that we want to collect query plans on.
   */
  public void setHashes(Set<String> hashes) {
    this.hashes = hashes;
  }
}
