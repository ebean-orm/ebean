package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.dbplatform.DbIdentity;
import com.avaje.ebean.config.dbplatform.DbTypeMap;

/**
 * Oracle platform specific DDL.
 */
public class Oracle10Ddl extends PlatformDdl {

  public Oracle10Ddl(DbTypeMap platformTypes, DbIdentity dbIdentity) {
    super(platformTypes, dbIdentity);
    this.dropTableIfExists = "drop table ";
    this.dropSequenceIfExists = "drop sequence ";
    this.dropTableCascade = " cascade constraints purge";
    this.maxConstraintNameLength = 30;
    this.foreignKeyRestrict = "";
  }

}
