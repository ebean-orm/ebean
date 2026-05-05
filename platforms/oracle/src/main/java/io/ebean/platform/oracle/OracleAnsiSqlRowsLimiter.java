package io.ebean.platform.oracle;

import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebean.config.dbplatform.SqlLimitResponse;
import io.ebean.config.dbplatform.SqlLimiter;

/**
 * Use ANSI offset rows syntax.
 */
final class OracleAnsiSqlRowsLimiter implements SqlLimiter {

  @Override
  public SqlLimitResponse limit(SqlLimitRequest request) {
    // Oracle does not support FOR UPDATE clause with limit offset
    return new SqlLimitResponse(request.ansiOffsetRows());
  }

}
