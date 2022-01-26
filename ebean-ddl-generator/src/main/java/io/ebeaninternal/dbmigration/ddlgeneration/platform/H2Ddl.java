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

  /**
   * Modify and return the column definition for autoincrement or identity definition.
   */
  @Override
  public String asIdentityColumn(String columnDefn, DdlIdentity identity) {
    return asIdentityStandardOptions(columnDefn, identity);
  }

  @Override
  protected String convertArrayType(String logicalArrayType) {
    int pos = logicalArrayType.indexOf('[');
    if (pos == -1) {
      return logicalArrayType;
    } else {
      // trim of the fallback varchar length
      return logicalArrayType.substring(0, pos) + " array";
    }
  }
}
