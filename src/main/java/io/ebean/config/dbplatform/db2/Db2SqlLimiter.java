package io.ebean.config.dbplatform.db2;

import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;

public class Db2SqlLimiter implements SqlLimiter {

  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {

    StringBuilder sb = new StringBuilder(512);
    sb.append("select ");
    if (request.isDistinct()) {
      sb.append("distinct ");
    }
    sb.append(request.getDbSql());

    int maxRows = request.getMaxRows();
    if (maxRows > 0) {
      sb.append(" ").append(NEW_LINE).append("FETCH FIRST ").append(maxRows).append(" ROWS ONLY");
    }
    // FIXME: There is no 'firstRow' support for DB2. Maybe we can use the MYS compatibility:
    // https://www.ibm.com/developerworks/community/blogs/SQLTips4DB2LUW/entry/limit_offset?lang=en

    String sql = request.getDbPlatform().completeSql(sb.toString(), request.getOrmQuery());
    return new SqlLimitResponse(sql, false);
  }
}
