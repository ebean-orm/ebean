package com.avaje.ebean.config.dbplatform;

/**
 * The resulting SQL from a SqlLimit process.
 */
public class SqlLimitResponse {

  final String sql;

  final boolean includesRowNumberColumn;

  /**
   * Create the response.
   */
  public SqlLimitResponse(String sql, boolean includesRowNumberColumn) {
    this.sql = sql;
    this.includesRowNumberColumn = includesRowNumberColumn;
  }

  /**
   * The final query sql with SQL limit statements added.
   */
  public String getSql() {
    return sql;
  }

  /**
   * Returns true if a ROW_NUMBER column is used in the query.
   */
  public boolean isIncludesRowNumberColumn() {
    return includesRowNumberColumn;
  }

}
