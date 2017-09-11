package io.ebean.dbmigration.ddlgeneration.platform;

import io.ebean.config.dbplatform.DatabasePlatform;

/**
 * Oracle platform specific DDL.
 */
public class Oracle10Ddl extends PlatformDdl {

  public Oracle10Ddl(DatabasePlatform platform) {
    super(platform);
    this.dropTableIfExists = "drop table ";
    this.dropSequenceIfExists = "drop sequence ";
    this.dropConstraintIfExists = "drop constraint";
    this.dropIndexIfExists = "drop index ";
    this.dropTableCascade = " cascade constraints purge";
    this.foreignKeyRestrict = "";
    this.alterColumn = "modify";
    this.addColumn = "add";
    this.columnSetNotnull = "not null";
    this.columnSetNull = "null";
    this.columnSetDefault = "default";
  }

}
