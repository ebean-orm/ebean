package com.avaje.ebean.config.dbplatform;

/**
 * Use top xx and start at xx to limit sql results. Based on
 * MsSqlServer2005SqlLimiter and LimitOffsetSqlLimiter
 */
public class SqlAnywhereLimiter implements SqlLimiter {

  public SqlLimitResponse limit(SqlLimitRequest request) {

    String dbSql = request.getDbSql();
    
    StringBuilder sb = new StringBuilder(60 + dbSql.length());

    int firstRow = request.getFirstRow();
    int maxRows = request.getMaxRows();

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
    sb.append(dbSql);

    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());

    return new SqlLimitResponse(sql, false);
  }

}
