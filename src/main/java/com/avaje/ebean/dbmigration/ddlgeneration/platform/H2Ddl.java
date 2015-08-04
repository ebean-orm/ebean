package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.DbTypeMap;

/**
 * H2 platform specific DDL.
 */
public class H2Ddl extends PlatformDdl {

  public H2Ddl(DbTypeMap platformTypes) {
    super(platformTypes, new H2HistoryDdl());
    this.foreignKeyRestrict = "on delete restrict on update restrict";
  }
}
