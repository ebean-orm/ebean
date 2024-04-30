package io.ebean.platform.sqlserver;

import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;

/**
 * Use ANSI offset rows syntax or top n - SQL Server 2012 onwards.
 */
final class SqlServerSqlLimiter implements SqlLimiter {

  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {
    int firstRow = request.getFirstRow();
    int maxRows = request.getMaxRows();
    if (firstRow < 1) {
      // just use top n
      final var buffer = request.selectDistinct();
      buffer.append("top ").append(maxRows).append(' ');
      buffer.append(request.getDbSql());
      return new SqlLimitResponse(buffer.toString());
    }
    final var buffer = request.selectDistinct();
    buffer.append(request.getDbSql());
    buffer.append(' ').append("offset");
    buffer.append(' ').append(firstRow).append(" rows");
    if (maxRows > 0) {
      buffer.append(" fetch next ").append(maxRows).append(" rows only");
    }
    return new SqlLimitResponse(buffer.toString());
  }

}
