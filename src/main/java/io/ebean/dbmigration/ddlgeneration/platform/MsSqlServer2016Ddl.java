package io.ebean.dbmigration.ddlgeneration.platform;

import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.dbmigration.ddlgeneration.DdlBuffer;
import io.ebean.dbmigration.ddlgeneration.DdlWrite;
import io.ebean.dbmigration.migration.AddHistoryTable;
import io.ebean.dbmigration.migration.AlterColumn;
import io.ebean.dbmigration.migration.DropHistoryTable;
import io.ebean.dbmigration.model.MTable;

import java.io.IOException;

/**
 * MS SQL Server 2016 platform specific DDL.
 */
public class MsSqlServer2016Ddl extends MsSqlServerDdl {

  public MsSqlServer2016Ddl(DatabasePlatform platform) {
    super(platform);
    this.historyDdl = new MsSqlServer2016HistoryDdl();
  }

}
