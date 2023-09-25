package io.ebean.platform.sqlanywhere;

import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;

/**
 * Use top xx and start at xx to limit sql results.
 */
public final class SqlAnywhereLimiter implements SqlLimiter {

  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {
    String dbSql = request.getDbSql();
    StringBuilder sb = new StringBuilder(60 + dbSql.length());
    /*
     * SELECT TOP xx START AT xx ... FROM ...
     */
    sb.append("select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }
    int maxRows = request.getMaxRows();
    if (maxRows > 0) {
      sb.append("top ").append(maxRows).append(' ');
    }
    int firstRow = request.getFirstRow();
    if (firstRow > 0) {
      sb.append("start at ").append(firstRow + 1).append(' ');
    }
    sb.append(dbSql);
    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());
    return new SqlLimitResponse(sql);
  }

}
