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
  static final char NEW_LINE = '\n';

  /**
   * The carriage return character.
   */
  static final char CARRIAGE_RETURN = '\r';

  /**
   * Add the SQL limiting statements around the query.
   */
  SqlLimitResponse limit(SqlLimitRequest request);
}
