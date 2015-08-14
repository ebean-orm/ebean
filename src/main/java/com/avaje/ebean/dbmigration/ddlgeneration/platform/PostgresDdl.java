package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.DbIdentity;
import com.avaje.ebean.config.dbplatform.DbTypeMap;

/**
 * Postgres specific DDL.
 */
public class PostgresDdl extends PlatformDdl {

  public PostgresDdl(DbTypeMap platformTypes, DbIdentity dbIdentity) {
    super(platformTypes, dbIdentity);
    this.historyDdl = new PostgresHistoryDdl();
    this.dropTableCascade = " cascade";
    this.columnSetType = "type ";
  }

  /**
   * Map bigint, integer and smallint into their equivalent serial types.
   */
  @Override
  public String asIdentityColumn(String columnDefn) {

    if ("bigint".equalsIgnoreCase(columnDefn)) {
      return "bigserial";
    }
    if ("integer".equalsIgnoreCase(columnDefn)) {
      return "serial";
    }
    if ("smallint".equalsIgnoreCase(columnDefn)) {
      return "smallserial";
    }
    return columnDefn;
  }
}
