package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.DatabasePlatform;

/**
 * H2 platform specific DDL.
 */
public class H2Ddl extends PlatformDdl {

  public H2Ddl(DatabasePlatform platform) {
    super(platform);
    this.historyDdl = new H2HistoryDdl();
  }

}
