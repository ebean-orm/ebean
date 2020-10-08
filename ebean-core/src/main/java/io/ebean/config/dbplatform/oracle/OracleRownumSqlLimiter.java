package io.ebean.config.dbplatform.oracle;

import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;

/**
 * Add ROWNUM column etc around SQL query to limit results.
 */
class OracleRownumSqlLimiter implements SqlLimiter {

  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {

    // select *
    // from ( select /*+ FIRST_ROWS(n) */ a.*, ROWNUM rnum
    // from ( your_query_goes_here,
    // with order by ) a
    // where ROWNUM <=
    // :MAX_ROW_TO_FETCH )
    // where rnum >= :MIN_ROW_TO_FETCH;

    String dbSql = request.getDbSql();

    StringBuilder sb = new StringBuilder(60 + dbSql.length());
    int firstRow = request.getFirstRow();
    int lastRow = request.getMaxRows();
    if (lastRow > 0) {
      lastRow += firstRow;
    }

    sb.append("select * from (select ");
    if (request.getMaxRows() > 0) {
      sb.append("/*+ FIRST_ROWS(").append(request.getMaxRows()).append(") */ ");
    }

    sb.append("a.*, rownum rn_ from (select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }
    sb.append(dbSql).append(") a ");
    if (lastRow > 0) {
      sb.append(" where rownum <= ").append(lastRow);
    }
    sb.append(") ");
    if (firstRow > 0) {
      sb.append(" where rn_ > ").append(firstRow);
    }

    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());
    return new SqlLimitResponse(sql);
  }

}
