package io.ebean.config.dbplatform.hana;

import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;

public class HanaSqlLimiter implements SqlLimiter {
  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {
    String dbSql = request.getDbSql();

    StringBuilder sb = new StringBuilder(50 + dbSql.length());
    sb.append("select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }

    sb.append(dbSql);

    int firstRow = request.getFirstRow();
    int maxRows = request.getMaxRows();

    if (maxRows > 0) {
      sb.append(" ").append("limit ").append(maxRows);
    }
    if (firstRow > 0) {
      sb.append(" ").append("offset ").append(firstRow);
    }
    // CHECKME: Roland Praml: as far as I see, this code does the same as 'LimotOffsetSqlLimiter'

    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());

    return new SqlLimitResponse(sql, false);
  }
}