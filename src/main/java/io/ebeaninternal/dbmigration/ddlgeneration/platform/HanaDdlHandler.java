package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.ServerConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.BaseDdlHandler;

public class HanaDdlHandler extends BaseDdlHandler {

  public HanaDdlHandler(ServerConfig serverConfig, PlatformDdl platformDdl) {
    super(serverConfig, platformDdl, new HanaTableDdl(serverConfig, platformDdl));
  }
}
