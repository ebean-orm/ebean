package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.annotation.ConstraintMode;
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
    this.alterColumn = "modify";
    this.columnSetNotnull = "not null";
    this.columnSetNull = "null";
    this.columnSetDefault = "default";
    this.identitySuffix = " generated always as identity";
  }

  @Override
  protected void appendForeignKeyOnUpdate(StringBuilder buffer, ConstraintMode mode) {
    // do nothing, no on update clause for oracle
  }

  @Override
  protected void appendForeignKeyMode(StringBuilder buffer, String onMode, ConstraintMode mode) {
    switch (mode) {
      case SET_NULL:
      case CASCADE:
        super.appendForeignKeyMode(buffer, onMode, mode);
      default:
        // do nothing, defaults to RESTRICT effectively
    }
  }

}
