package io.ebean.config.dbplatform;

/**
 * Adds LIMIT OFFSET clauses to a SQL query.
 */
public final class LimitOffsetSqlLimiter implements SqlLimiter {

  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {
    final var buffer = request.selectDistinctOnSql();
    int maxRows = request.getMaxRows();
    if (maxRows > 0) {
      buffer.append(" limit ").append(maxRows);
    }
    int firstRow = request.getFirstRow();
    if (firstRow > 0) {
      buffer.append(" offset ").append(firstRow);
    }
    return new SqlLimitResponse(request.getDbPlatform().completeSql(buffer.toString(), request.getOrmQuery()));
  }

}
