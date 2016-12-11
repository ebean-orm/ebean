package io.ebean.config.dbplatform.db2;

import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;

public class Db2SqlLimiter implements SqlLimiter {

  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {

    StringBuilder sb = new StringBuilder(512);
    sb.append("select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }
    sb.append(request.getDbSql());

    int maxRows = request.getMaxRows();
    if (maxRows > 0) {
      sb.append(" ").append(NEW_LINE).append("FETCH FIRST ").append(maxRows).append(" ROWS ONLY");
    }

    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());
    return new SqlLimitResponse(sql, false);
  }
}
