package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;

public class YugabyteDdl extends PostgresDdl {

  public YugabyteDdl(DatabasePlatform platform) {
    super(platform);
    this.historyDdl = new YugabyteHistoryDdl();
  }
}
