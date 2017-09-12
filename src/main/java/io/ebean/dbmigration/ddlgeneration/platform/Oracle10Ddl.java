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
    this.columnDropDefault = "default null";
    this.dropTableCascade = " cascade constraints purge";
    this.foreignKeyRestrict = "";
    this.alterColumn = "modify";
    this.addColumn = "add";
    this.columnSetNotnull = "not null";
    this.columnSetNull = "null";
    this.columnSetDefault = "default";
  }

  public String alterTableAddUniqueConstraint(String tableName, String uqName, String[] columns, String[] nullableColumns) {
    if (nullableColumns == null || nullableColumns.length == 0) {
      return super.alterTableAddUniqueConstraint(tableName, uqName, columns, nullableColumns);
    } else {
      // Hmm: https://stackoverflow.com/questions/11893134/oracle-create-unique-index-but-ignore-nulls
      return "-- NOT YET IMPLEMENTED: " + super.alterTableAddUniqueConstraint(tableName, uqName, columns, nullableColumns);
    }
  }
}
