package io.ebean.config.dbplatform;

/**
 * Adds LIMIT OFFSET clauses to a SQL query.
 */
public final class LimitOffsetSqlLimiter implements SqlLimiter {

  /**
   * LIMIT keyword.
   */
  private static final String LIMIT = "limit";

  /**
   * OFFSET keyword.
   */
  private static final String OFFSET = "offset";

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
      sb.append(" ").append(LIMIT).append(" ").append(maxRows);
    }
    int firstRow = request.getFirstRow();
    if (firstRow > 0) {
      sb.append(" ").append(OFFSET).append(" ");
      sb.append(firstRow);
    }
    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());
    return new SqlLimitResponse(sql);
  }

}
