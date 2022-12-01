package io.ebean.platform.hana;

import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;

public final class HanaSqlLimiter implements SqlLimiter {

  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {
    String dbSql = request.getDbSql();
    StringBuilder sb = new StringBuilder(50 + dbSql.length());
    sb.append("select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }
    sb.append(dbSql);
    int maxRows = request.getMaxRows();
    if (maxRows > 0) {
      sb.append(" ").append("limit ").append(maxRows);
    }
    int firstRow = request.getFirstRow();
    if (firstRow > 0) {
      sb.append(" ").append("offset ");
      sb.append(firstRow);
    }
    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());
    return new SqlLimitResponse(sql);
  }
}
