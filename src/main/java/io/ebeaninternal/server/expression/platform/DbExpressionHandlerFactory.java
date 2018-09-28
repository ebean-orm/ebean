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
        return new PostgresDbExpression();
      case MYSQL:
        return new MySqlDbExpression();
      case ORACLE:
        return new OracleDbExpression();
      case SQLSERVER16:
      case SQLSERVER17:
      case SQLSERVER:
        return new SqlServerDbExpression();
      default:
        return new BasicDbExpression();
    }
  }
}
