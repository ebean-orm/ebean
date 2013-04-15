package com.avaje.ebean.config.dbplatform;

/**
 * Use top xx and start at xx to limit sql results. Based on
 * MsSqlServer2005SqlLimiter and LimitOffsetSqlLimiter
 */
public class SqlAnywhereLimiter implements SqlLimiter {

  public SqlLimitResponse limit(SqlLimitRequest request) {

    StringBuilder sb = new StringBuilder(500);

    int firstRow = request.getFirstRow();
    int maxRows = request.getMaxRows();
    if (maxRows > 0) {
      // fetch 1 more than we return so that
      // we know if more rows are available
      maxRows = maxRows + 1;
    }

    /*
     * SELECT TOP xx START AT xx ... FROM ...
     */
    sb.append("select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }
    if (maxRows > 0) {
      sb.append("top ").append(maxRows).append(" ");
    }
    if (firstRow > 0) {
      sb.append("start at ").append(firstRow + 1).append(" ");
    }
    sb.append(request.getDbSql());

    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());

    return new SqlLimitResponse(sql, false);
  }

}
