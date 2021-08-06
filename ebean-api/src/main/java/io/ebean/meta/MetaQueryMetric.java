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
   * Migrate to type().
   */
  @Deprecated
  default Class<?> getType() {
    return type();
  }

  /**
   * The label for the query (can be null).
   */
  String label();

  /**
   * Migrate to label().
   */
  @Deprecated
  default String getLabel() {
    return label();
  }

  /**
   * The actual SQL of the query.
   */
  String sql();

  /**
   * Migrate to sql().
   */
  @Deprecated
  default String getSql() {
    return sql();
  }

  /**
   * Return the hash of the sql.
   */
  long sqlHash();

  /**
   * Migrate to sqlHash().
   */
  @Deprecated
  default long getSqlHash() {
    return sqlHash();
  }
}
