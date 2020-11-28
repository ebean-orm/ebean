package io.ebean.config.dbplatform;

/**
 * Adds ANSI based OFFSET FETCH NEXT clauses to a SQL query.
 */
public class BasicSqlAnsiLimiter implements BasicSqlLimiter {

  @Override
  public String limit(String dbSql, int firstRow, int maxRows) {

    StringBuilder sb = new StringBuilder(50 + dbSql.length());

    sb.append(dbSql);
    if (firstRow > 0) {
      sb.append(" ").append("offset");
      sb.append(" ").append(firstRow).append(" rows");
    }
    if (maxRows > 0) {
      sb.append(" fetch next ").append(maxRows).append(" rows only");
    }
    return sb.toString();
  }

}
