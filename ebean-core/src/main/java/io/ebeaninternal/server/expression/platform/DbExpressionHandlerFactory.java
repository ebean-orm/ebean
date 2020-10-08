package io.ebeaninternal.server.expression.platform;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;


public class DbExpressionHandlerFactory {

  /**
   * Create and return the appropriate platform specific handing of expressions.
   */
  public static DbExpressionHandler from(DatabasePlatform databasePlatform) {

    Platform platform = databasePlatform.getPlatform();
    switch (platform) {
      case H2:
        return new H2DbExpression();
      case POSTGRES:
      case POSTGRES9:
        return new PostgresDbExpression();
      case MARIADB:
        return new MariaDbExpression();
      case MYSQL55:
      case MYSQL:
        return new MySqlDbExpression();
      case ORACLE:
      case ORACLE11:
        return new OracleDbExpression();
      case SQLSERVER16:
      case SQLSERVER17:
      case SQLSERVER:
        return new SqlServerDbExpression();
      case HANA:
        return new HanaDbExpression();
      default:
        return new BasicDbExpression();
    }
  }
}
