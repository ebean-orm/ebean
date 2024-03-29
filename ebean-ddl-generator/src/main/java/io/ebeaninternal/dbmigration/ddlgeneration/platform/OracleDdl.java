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
    this.columnDropDefault = "default null"; // dropping default is not supported. See:
    // https://stackoverflow.com/questions/8481532/how-do-i-remove-the-default-value-from-a-column-in-oracle
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
  public String dropSequence(String sequenceName) {
    return ignoreError(-2289, super.dropSequence(sequenceName));
  }

  @Override
  public String alterTableDropConstraint(String tableName, String constraintName) {
    return ignoreError(-2443, super.alterTableDropConstraint(tableName, constraintName));
  }

  @Override
  public String alterTableDropUniqueConstraint(String tableName, String uniqueConstraintName) {
    return ignoreError(-2443, super.alterTableDropUniqueConstraint(tableName, uniqueConstraintName));
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

  /**
   * generates anonymous pl/sql block that catches expected error so that we can
   * effectively do a 'drop if exists'
   */
  private String ignoreError(int errorNr, String statement) {
    StringBuilder sb = new StringBuilder();
    sb.append("delimiter $$\n")
        .append("declare\n")
        .append("  expected_error exception;\n")
        .append("  pragma exception_init(expected_error, ").append(errorNr).append(");\n")
        .append("begin\n")
        .append("  execute immediate '").append(statement).append("';\n")
        .append("exception\n")
        .append("  when expected_error then null;\n")
        .append("end;\n$$");
    return sb.toString();
  }
}
