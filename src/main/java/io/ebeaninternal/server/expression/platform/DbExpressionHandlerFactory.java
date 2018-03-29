package io.ebeaninternal.server.expression.platform;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;


public class DbExpressionHandlerFactory {

  /**
   * Create and return the appropriate platform specific handing of expressions.
   */
  public static DbExpressionHandler from(DatabasePlatform databasePlatform) {

    Platform platform = databasePlatform.getPlatform();
    String concatOperator = databasePlatform.getConcatOperator();
    switch (platform) {
      case H2:
        return new H2DbExpression(concatOperator);
      case POSTGRES:
        return new PostgresDbExpression(concatOperator);
      case MYSQL:
        return new MySqlDbExpression(concatOperator);
      case ORACLE:
        return new OracleDbExpression(concatOperator);
      case SQLSERVER:
        return new SqlServerDbExpression(concatOperator);
      default:
        return new BasicDbExpression(concatOperator);
    }
  }
}
