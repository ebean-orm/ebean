package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.Query;

/**
 * The request object for the query that can have sql limiting applied to it
 * (such as a LIMIT OFFSET clause).
 */
public interface SqlLimitRequest {

  /**
   * Return true if the query uses distinct.
   */
  boolean isDistinct();

  /**
   * Return the first row value.
   */
  int getFirstRow();

  /**
   * Return the max rows for this query.
   */
  int getMaxRows();

  /**
   * Return the sql query.
   */
  String getDbSql();

  /**
   * Return the orderBy clause of the sql query.
   */
  String getDbOrderBy();

  /**
   * return the query
   */
  Query<?> getOrmQuery();

  /**
   * return the database platform
   */
  DatabasePlatform getDbPlatform();
}
