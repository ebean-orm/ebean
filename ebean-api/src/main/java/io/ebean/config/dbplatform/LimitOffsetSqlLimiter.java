package io.ebean.config.dbplatform;

/**
 * Adds LIMIT OFFSET clauses to a SQL query.
 */
public final class LimitOffsetSqlLimiter implements SqlLimiter {

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
      sb.append(" limit ").append(maxRows);
    }
    int firstRow = request.getFirstRow();
    if (firstRow > 0) {
      sb.append(" offset ").append(firstRow);
    }
    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());
    return new SqlLimitResponse(sql);
  }

}
