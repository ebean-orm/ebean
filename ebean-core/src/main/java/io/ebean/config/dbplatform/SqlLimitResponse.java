package io.ebean.config.dbplatform;

/**
 * The resulting SQL from a SqlLimit process.
 */
public class SqlLimitResponse {

  final String sql;

  /**
   * Create the response.
   */
  public SqlLimitResponse(String sql) {
    this.sql = sql;
  }

  /**
   * The final query sql with SQL limit statements added.
   */
  public String getSql() {
    return sql;
  }

}
