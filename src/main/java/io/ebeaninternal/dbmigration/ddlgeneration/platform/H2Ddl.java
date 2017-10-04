package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;

/**
 * H2 platform specific DDL.
 */
public class H2Ddl extends PlatformDdl {

  public H2Ddl(DatabasePlatform platform) {
    super(platform);
    this.historyDdl = new H2HistoryDdl();
  }

  @Override
  protected String convertArrayType(String logicalArrayType) {
    return "array";
  }
}
