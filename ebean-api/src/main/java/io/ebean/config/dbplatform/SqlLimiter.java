package io.ebean.config.dbplatform;

/**
 * Adds SQL limiting to a query (such as LIMIT OFFSET).
 */
public interface SqlLimiter {

  /**
   * Add the SQL limiting statements around the query.
   */
  SqlLimitResponse limit(SqlLimitRequest request);
}
