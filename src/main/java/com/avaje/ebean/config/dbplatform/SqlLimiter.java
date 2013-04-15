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
  public static final char NEW_LINE = '\n';

  /**
   * The carriage return character.
   */
  public static final char CARRIAGE_RETURN = '\r';

  /**
   * Add the SQL limiting statements around the query.
   */
  public SqlLimitResponse limit(SqlLimitRequest request);
}
