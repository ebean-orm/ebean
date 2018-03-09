package io.ebean.config.dbplatform.sqlserver;

/**
 * SQL Server platform using the older ROW_NUMBER() mechanism.
 */
public class SqlServer2005Platform extends SqlServer16Platform {

  public SqlServer2005Platform() {
    this.sqlLimiter = new SqlServer2005SqlLimiter();
  }
}
