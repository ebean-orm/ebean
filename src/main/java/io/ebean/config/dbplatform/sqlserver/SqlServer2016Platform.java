package io.ebean.config.dbplatform.sqlserver;

import io.ebean.dbmigration.ddlgeneration.platform.MsSqlServer2016Ddl;

/**
 * @author Vilmos Nagy  <vilmos.nagy@outlook.com>
 */
public class SqlServer2016Platform extends SqlServerPlatform {

  public SqlServer2016Platform() {
    super();
    this.historySupport = new SqlServer2016HistorySupport();
    this.platformDdl = new MsSqlServer2016Ddl(this);
  }
}
