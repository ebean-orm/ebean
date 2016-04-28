package com.avaje.ebeaninternal.api;

import java.sql.PreparedStatement;

import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlQueryListener;

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
   * Return the queryListener.
   */
  SqlQueryListener getListener();

  /**
   * Return the first row to fetch.
   */
  int getFirstRow();

  /**
   * Return the maximum number of rows to fetch.
   */
  int getMaxRows();

  /**
   * Return the key property for maps.
   */
  String getMapKey();

  /**
   * Return the query timeout.
   */
  int getTimeout();

  /**
   * Return the hint for Statement.setFetchSize().
   */
  int getBufferFetchSizeHint();

  /**
   * Return true if this is a future fetch type query.
   */
  boolean isFutureFetch();

  /**
   * Set to true if this is a future fetch type query.
   */
  void setFutureFetch(boolean futureFetch);

  /**
   * Set the PreparedStatement for the purposes of supporting cancel.
   */
  void setPreparedStatement(PreparedStatement pstmt);

  /**
   * Return true if the query has been cancelled.
   */
  boolean isCancelled();
}
