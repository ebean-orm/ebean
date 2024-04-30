package io.ebean.config.dbplatform;

public final class AnsiSqlRowsLimiter implements SqlLimiter {

  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {
    final var ansiSql = request.ansiOffsetRows();
    final var sql = request.getDbPlatform().completeSql(ansiSql, request.getOrmQuery());
    return new SqlLimitResponse(sql);
  }
}
