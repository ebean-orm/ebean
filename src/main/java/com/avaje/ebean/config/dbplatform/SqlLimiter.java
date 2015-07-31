package com.avaje.ebean.config.dbplatform;

/**
 * Adds SQL limiting to a query (such as LIMIT OFFSET).
 */
public interface SqlLimiter {

  /**
   * the new line character used.
   * <p>
   * Note that this is removed for logging sql to the transaction log.
   * </p>
   */
  char NEW_LINE = '\n';

  /**
   * Add the SQL limiting statements around the query.
   */
  SqlLimitResponse limit(SqlLimitRequest request);
}
