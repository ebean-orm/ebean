package io.ebeaninternal.server.query;

import io.ebean.annotation.Platform;

public final class PlatformQueryPlan {

  private static QueryPlanLogger explainLogger = new QueryPlanLoggerExplain();

  private static QueryPlanLogger postgresLogger = new QueryPlanLoggerPostgres();

  private static QueryPlanLogger sqlServerLogger = new QueryPlanLoggerSqlServer();

  private static QueryPlanLogger oracleLogger = new QueryPlanLoggerOracle();

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
