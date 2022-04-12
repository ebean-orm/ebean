package io.ebean.meta;

/**
 * Query execution metrics.
 */
public interface MetaQueryMetric extends MetaTimedMetric {

  /**
   * The type of entity or DTO bean.
   */
  Class<?> type();

  /**
   * The label for the query (can be null).
   */
  String label();

  /**
   * The actual SQL of the query.
   */
  String sql();

  /**
   * Return the hash of the plan.
   */
  String hash();

}
