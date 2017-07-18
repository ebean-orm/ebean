package io.ebean.config.dbplatform.sqlserver;

/**
 * SQL Server platform using the ViewHistory for
 * versions older than SQL Server 2016
 */
public class SqlServerViewHistoryPlatform extends SqlServerPlatform {

  public SqlServerViewHistoryPlatform() {
    this.historySupport = new SqlServerViewHistorySupport();
  }
}
