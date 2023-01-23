package io.ebeaninternal.server.expression.platform;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;


public final class DbExpressionHandlerFactory {

  /**
   * Create and return the appropriate platform specific handing of expressions.
   */
  public static DbExpressionHandler from(DatabasePlatform databasePlatform) {
    Platform platform = databasePlatform.platform().base();
    switch (platform) {
      case H2:
        return new H2DbExpression();
      case POSTGRES:
      case YUGABYTE:
      case COCKROACH:
        return new PostgresDbExpression();
      case MARIADB:
        return new MariaDbExpression();
      case MYSQL:
        return new MySqlDbExpression();
      case ORACLE:
        return new OracleDbExpression();
      case DB2:
        return new Db2DbExpression();
      case SQLSERVER:
        return new SqlServerDbExpression();
      case HANA:
        return new HanaDbExpression();
      default:
        return new BasicDbExpression();
    }
  }
}
