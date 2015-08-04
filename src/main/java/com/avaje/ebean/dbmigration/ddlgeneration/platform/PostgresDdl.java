package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.DbTypeMap;

/**
 * Postgres specific DDL.
 */
public class PostgresDdl extends PlatformDdl {

  public PostgresDdl(DbTypeMap platformTypes) {
    super(platformTypes, new PostgresHistoryDdl());
    this.foreignKeyRestrict = "on delete restrict on update restrict";
  }

  /**
   * Map bigint, integer and smallint into their equivalent serial types.
   */
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
