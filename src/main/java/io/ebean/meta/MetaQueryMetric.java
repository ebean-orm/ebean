package io.ebean.meta;

/**
 * Query execution metrics.
 */
public interface MetaQueryMetric extends MetaTimedMetric {

  /**
   * The type of entity or DTO bean.
   */
  Class<?> getType();

  /**
   * The label for the query (can be null).
   */
  String getLabel();

  /**
   * The actual SQL of the query.
   */
  String getSql();

}
