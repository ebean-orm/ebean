package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DatabaseBuilder;
import io.ebeaninternal.dbmigration.ddlgeneration.BaseDdlHandler;

public class ClickHouseDdlHandler extends BaseDdlHandler {

  public ClickHouseDdlHandler(DatabaseBuilder.Settings config, PlatformDdl platformDdl) {
    super(config, platformDdl, new ClickHouseTableDdl(config, platformDdl));
  }
}
