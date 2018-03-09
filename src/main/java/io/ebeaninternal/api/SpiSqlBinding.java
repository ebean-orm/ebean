package io.ebeaninternal.api;

/**
 * SQL query binding (for SqlQuery and DtoQuery).
 */
public interface SpiSqlBinding {

  /**
   * Return the named or positioned parameters.
   */
  BindParams getBindParams();

  /**
   * return the query.
   */
  String getQuery();

  /**
   * Return the label (to collect metrics on when set).
   */
  String getLabel();

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
