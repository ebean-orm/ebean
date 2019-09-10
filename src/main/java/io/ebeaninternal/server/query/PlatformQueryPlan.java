package io.ebeaninternal.server.query;

import io.ebean.annotation.Platform;

final class PlatformQueryPlan {

  private static final QueryPlanLogger explainLogger = new QueryPlanLoggerExplain();

  private static final QueryPlanLogger postgresLogger = new QueryPlanLoggerPostgres();

  private static final QueryPlanLogger sqlServerLogger = new QueryPlanLoggerSqlServer();

  private static final QueryPlanLogger oracleLogger = new QueryPlanLoggerOracle();

  /**
   * Returns the logger to log query plans for the given platform.
   */
  public static QueryPlanLogger getLogger(Platform platform) {

    switch (platform) {
      case POSTGRES:
        return postgresLogger;

      case SQLSERVER:
      case SQLSERVER16:
      case SQLSERVER17:
        return sqlServerLogger;

      case ORACLE:
        return oracleLogger;

      default:
        return explainLogger;
    }
  }
}
