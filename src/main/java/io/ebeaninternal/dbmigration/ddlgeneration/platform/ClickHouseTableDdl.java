package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.ServerConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.migration.CreateTable;

public class ClickHouseTableDdl extends BaseTableDdl {

  public ClickHouseTableDdl(ServerConfig serverConfig, PlatformDdl platformDdl) {
    super(serverConfig, platformDdl);
  }

  @Override
  protected void writePrimaryKeyConstraint(DdlBuffer buffer, String pkName, String[] pkColumns) {
    // do nothing
  }

  @Override
  protected void writeCompoundUniqueConstraints(DdlBuffer apply, CreateTable createTable) {
    // do nothing
  }

  @Override
  protected void writeUniqueConstraints(DdlBuffer apply, CreateTable createTable) {
    // do nothing
  }

}
