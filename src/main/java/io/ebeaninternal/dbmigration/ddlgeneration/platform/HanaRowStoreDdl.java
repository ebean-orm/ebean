package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;

public class HanaRowStoreDdl extends AbstractHanaDdl {

  public HanaRowStoreDdl(DatabasePlatform platform) {
    super(platform);
    this.createTable = "create row table";
  }
}
