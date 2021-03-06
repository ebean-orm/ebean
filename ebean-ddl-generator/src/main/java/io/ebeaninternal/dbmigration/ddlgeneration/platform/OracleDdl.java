package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.annotation.ConstraintMode;
import io.ebean.config.dbplatform.DatabasePlatform;

/**
 * Oracle platform specific DDL.
 */
public class OracleDdl extends PlatformDdl {

  public OracleDdl(DatabasePlatform platform) {
    super(platform);
    this.dropTableIfExists = "drop table ";
    this.dropSequenceIfExists = "drop sequence ";
    this.dropConstraintIfExists = "drop constraint";
    this.dropIndexIfExists = "drop index ";
    this.dropTableCascade = " cascade constraints purge";
    this.addColumn = "add";
    this.alterColumn = "modify";
    this.columnSetNotnull = "not null";
    this.columnSetNull = "null";
    this.columnSetDefault = "default";
    this.identitySuffix = " generated by default as identity";
  }

  @Override
  public String alterTableAddUniqueConstraint(String tableName, String uqName, String[] columns, String[] nullableColumns) {
    if (nullableColumns == null || nullableColumns.length == 0) {
      return super.alterTableAddUniqueConstraint(tableName, uqName, columns, nullableColumns);
    } else {
      // Hmm: https://stackoverflow.com/questions/11893134/oracle-create-unique-index-but-ignore-nulls
      return "-- NOT YET IMPLEMENTED: " + super.alterTableAddUniqueConstraint(tableName, uqName, columns, nullableColumns);
    }
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

  /**
   * Modify and return the column definition for autoincrement or identity definition.
   */
  @Override
  public String asIdentityColumn(String columnDefn, DdlIdentity identity) {
    return asIdentityStandardOptions(columnDefn, identity);
  }

}
