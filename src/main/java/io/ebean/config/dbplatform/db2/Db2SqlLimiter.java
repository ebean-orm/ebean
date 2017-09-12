package io.ebean.config.dbplatform.db2;

import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;

public class Db2SqlLimiter implements SqlLimiter {

  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {

    String dbSql = request.getDbSql();
    StringBuilder sb = new StringBuilder(150 + dbSql.length());
    
    int firstRow = request.getFirstRow();
    int maxRows = request.getMaxRows();

    sb.append("select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }
    sb.append(request.getDbSql()).append(" ").append(NEW_LINE);

    if (firstRow > 1) {
      sb.append("OFFSET ").append(firstRow).append(" ROWS ");
    }

    if (maxRows > 0) {
      sb.append("FETCH FIRST ").append(maxRows).append(" ROWS ONLY");
    }
    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());
    return new SqlLimitResponse(sql, false);

  }
}
