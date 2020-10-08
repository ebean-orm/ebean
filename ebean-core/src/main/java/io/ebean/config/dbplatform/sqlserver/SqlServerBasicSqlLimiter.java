package io.ebean.config.dbplatform.sqlserver;

import io.ebean.config.dbplatform.BasicSqlLimiter;

/**
 * SQL Server 2012 style limiter for raw sql.
 */
public class SqlServerBasicSqlLimiter implements BasicSqlLimiter {

  @Override
  public String limit(String dbSql, int firstRow, int maxRows) {

    StringBuilder sb = new StringBuilder(50 + dbSql.length());
    sb.append(dbSql);
    if (!dbSql.toLowerCase().contains("order by")) {
      sb.append(" order by 1");
    }
    sb.append(" ").append("offset");
    sb.append(" ").append(firstRow).append(" rows");
    if (maxRows > 0) {
      sb.append(" fetch next ").append(maxRows).append(" rows only");
    }
    return sb.toString();
  }

}
