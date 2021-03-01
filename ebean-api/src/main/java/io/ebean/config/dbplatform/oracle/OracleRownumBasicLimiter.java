package io.ebean.config.dbplatform.oracle;

import io.ebean.config.dbplatform.BasicSqlLimiter;

/**
 * Row limiter for Oracle 9,10,11 using rownum.
 */
public class OracleRownumBasicLimiter implements BasicSqlLimiter {

  @Override
  public String limit(String dbSql, int firstRow, int maxRows) {
    if (firstRow < 1 && maxRows < 1) {
      return dbSql;
    }
    StringBuilder sb = new StringBuilder(60 + dbSql.length());
    int lastRow = maxRows;
    if (lastRow > 0) {
      lastRow += firstRow;
    }
    sb.append("select * from (select ");
    if (maxRows > 0) {
      sb.append("/*+ FIRST_ROWS(").append(maxRows).append(") */ ");
    }
    sb.append("a.*, rownum rn_ from (");
    sb.append(dbSql).append(") a ");
    if (lastRow > 0) {
      sb.append(" where rownum <= ").append(lastRow);
    }
    sb.append(") ");
    if (firstRow > 0) {
      sb.append(" where rn_ > ").append(firstRow);
    }
    return sb.toString();
  }
}
