package io.ebean.platform.db2;

import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;


final class DB2RowNumberSqlLimiter implements SqlLimiter {

  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {
    String dbSql = request.getDbSql();

    StringBuilder sb = new StringBuilder(120 + dbSql.length());
    int firstRow = request.getFirstRow();
    int lastRow = request.getMaxRows();
    if (lastRow > 0) {
      lastRow += firstRow;
    }

    sb.append("select * from (select row_number() over() a.*, rn from (select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }
    sb.append(dbSql).append(") a ");
    if (lastRow > 0) {
      sb.append(" where rn <= ").append(lastRow);
    }
    sb.append(") ");
    if (firstRow > 0) {
      sb.append(" where rn > ").append(firstRow);
    }

    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());
    return new SqlLimitResponse(sql);
  }

}
