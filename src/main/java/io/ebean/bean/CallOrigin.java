package io.ebean.bean;

/**
 * A call origin for query execution profiling to collect graph use (for query tuning).
 */
public interface CallOrigin {

  /**
   * Return the top element. Typically the top stack element with class and line.
   */
  String getTopElement();

  /**
   * Return the full description of the call origin.
   */
  String getFullDescription();

  /**
   * Compute and return an origin key based on the query hash.
   */
  String getOriginKey(int queryHash);
}
