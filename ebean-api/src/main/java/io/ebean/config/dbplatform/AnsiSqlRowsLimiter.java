package io.ebean.config.dbplatform;

public final class AnsiSqlRowsLimiter implements SqlLimiter {

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
    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());
    return new SqlLimitResponse(sql);
  }
}
