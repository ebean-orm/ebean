package io.ebeaninternal.api;

import io.ebean.SqlQuery;

/**
 * SQL query - Internal extension to SqlQuery.
 */
public interface SpiSqlQuery extends SqlQuery {

  /**
   * Return the named or positioned parameters.
   */
  BindParams getBindParams();

  /**
   * return the query.
   */
  String getQuery();

  /**
   * Return the first row to fetch.
   */
  int getFirstRow();

  /**
   * Return the maximum number of rows to fetch.
   */
  int getMaxRows();

  /**
   * Return the query timeout.
   */
  int getTimeout();

  /**
   * Return the hint for Statement.setFetchSize().
   */
  int getBufferFetchSizeHint();

}
