package io.ebean.config.dbplatform;

/**
 * Simple SQL limiter for use with SqlQuery.
 */
public interface BasicSqlLimiter {

  /**
   * Add basic offset/limit clause to SqlQuery query.
   */
  String limit(String dbSql, int firstRow, int maxRows);
}
