package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.DatabaseConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.BaseDdlHandler;

public class HanaDdlHandler extends BaseDdlHandler {

  public HanaDdlHandler(DatabaseConfig config, PlatformDdl platformDdl) {
    super(config, platformDdl, new HanaTableDdl(config, platformDdl));
  }
}
