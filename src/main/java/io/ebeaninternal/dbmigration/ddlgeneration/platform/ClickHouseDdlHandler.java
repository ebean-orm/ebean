package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.ServerConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.BaseDdlHandler;

public class ClickHouseDdlHandler extends BaseDdlHandler {

  public ClickHouseDdlHandler(ServerConfig serverConfig, PlatformDdl platformDdl) {
    super(serverConfig, platformDdl, new ClickHouseTableDdl(serverConfig, platformDdl));
  }
}
