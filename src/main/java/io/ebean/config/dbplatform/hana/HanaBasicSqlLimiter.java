package io.ebean.config.dbplatform.hana;

import io.ebean.config.dbplatform.BasicSqlLimiter;

public class HanaBasicSqlLimiter implements BasicSqlLimiter {
  @Override
  public String limit(String dbSql, int firstRow, int maxRows) {
    StringBuilder sb = new StringBuilder(50 + dbSql.length());

    sb.append(dbSql);

    if (maxRows > 0) {
      sb.append(" ").append("limit");
      sb.append(" ").append(maxRows);

      if (firstRow > 0) {
        sb.append(" ").append("offset").append(" ");
        sb.append(firstRow);
      }
    }

    return sb.toString();
  }
}