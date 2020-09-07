package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.BaseDdlHandler;

public class ClickHouseDdlHandler extends BaseDdlHandler {

  public ClickHouseDdlHandler(DatabaseConfig config, PlatformDdl platformDdl) {
    super(config, platformDdl, new ClickHouseTableDdl(config, platformDdl));
  }
}
