package com.avaje.ebean.config.dbplatform.sqlserver;

import com.avaje.ebean.config.dbplatform.SqlLimitRequest;
import com.avaje.ebean.config.dbplatform.SqlLimitResponse;
import com.avaje.ebean.config.dbplatform.SqlLimiter;

/**
 * Use ANSI offset rows syntax or top n.
 */
public class SqlServer2012SqlLimiter implements SqlLimiter {

  public SqlServer2012SqlLimiter() {
  }

  public SqlLimitResponse limit(SqlLimitRequest request) {

    String dbSql = request.getDbSql();
    StringBuilder sb = new StringBuilder(50 + dbSql.length());

    int firstRow = request.getFirstRow();
    int maxRows = request.getMaxRows();

    if (firstRow < 1) {
      // just use top n
      sb.append("select ");
      if (request.isDistinct()) {
        sb.append("distinct ");
      }
      sb.append("top ").append(maxRows).append(" ");
      sb.append(dbSql);
      return new SqlLimitResponse(sb.toString(), false);
    }

    sb.append("select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }

    sb.append(dbSql);
    if (firstRow > 0) {
      sb.append(" ").append("offset");
      sb.append(" ").append(firstRow).append(" rows");
    }
    if (maxRows > 0) {
      sb.append(" fetch next ").append(maxRows).append(" rows only");
    }
    String sql = sb.toString();
    return new SqlLimitResponse(sql, false);
  }

}
