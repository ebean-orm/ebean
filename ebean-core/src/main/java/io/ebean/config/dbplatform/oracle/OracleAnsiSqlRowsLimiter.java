package io.ebean.config.dbplatform.oracle;

import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;

/**
 * Use ANSI offset rows syntax.
 */
class OracleAnsiSqlRowsLimiter implements SqlLimiter {

  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {

    String dbSql = request.getDbSql();
    StringBuilder sb = new StringBuilder(50 + dbSql.length());
    int firstRow = request.getFirstRow();
    int maxRows = request.getMaxRows();

    sb.append("select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }
    sb.append(dbSql);
    if (firstRow > 0) {
      sb.append(" offset ").append(firstRow).append(" rows");
    }
    if (maxRows > 0) {
      sb.append(" fetch next ").append(maxRows).append(" rows only");
    }
    return new SqlLimitResponse(sb.toString());
  }

}
