package io.ebean.platform.db2;

import io.ebean.config.dbplatform.BasicSqlLimiter;

/**
 * Row limiter for Oracle 9,10,11 using rownum.
 */
final class DB2RowNumberBasicLimiter implements BasicSqlLimiter {

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
    sb.append("select * from (select row_number() over() a.*, rn from (");
    sb.append(dbSql).append(") a ");
    if (lastRow > 0) {
      sb.append(" where rn <= ").append(lastRow);
    }
    sb.append(") ");
    if (firstRow > 0) {
      sb.append(" where rn_ > ").append(firstRow);
    }
    return sb.toString();
  }
}
