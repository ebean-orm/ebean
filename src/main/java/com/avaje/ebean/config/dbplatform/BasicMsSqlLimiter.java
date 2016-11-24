package com.avaje.ebean.config.dbplatform;

/**
 * Adds MS-SQL Limiter support.
 * Introduced in MS SQL Server 2012 enhancing the ORDER BY clause in with OFFSET/FETCH support.
 */
public class BasicMsSqlLimiter implements BasicSqlLimiter {

  @Override
  public String limit(String dbSql, int firstRow, int maxRows) {

    StringBuilder sb = new StringBuilder(50 + dbSql.length());
    sb.append(dbSql);
    if (!dbSql.toLowerCase().contains("order by")) {
      sb.append(" ORDER BY 1 ");
    }
    sb.append(" ").append("offset");
    sb.append(" ").append(firstRow).append(" rows");
    if (maxRows > 0) {
      sb.append(" fetch next ").append(maxRows).append(" rows only");
    }
    return sb.toString();
  }

}
