package io.ebean.platform.oracle;

import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;

/**
 * Use ANSI offset rows syntax.
 */
final class OracleAnsiSqlRowsLimiter implements SqlLimiter {

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
    if (firstRow > 0) {
      sb.append(" offset ").append(firstRow).append(" rows");
    }
    int maxRows = request.getMaxRows();
    if (maxRows > 0) {
      sb.append(" fetch next ").append(maxRows).append(" rows only");
    }
    // Oracle does not support FOR UPDATE clause with limit offset
    return new SqlLimitResponse(sb.toString());
  }

}
