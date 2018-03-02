package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import java.io.IOException;

import io.ebean.annotation.ConstraintMode;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;

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
    this.alterColumn = "modify";
    this.addColumn = "add";
    this.columnSetNotnull = "not null";
    this.columnSetNull = "null";
    this.columnSetDefault = "default";
    this.identitySuffix = " generated always as identity";
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
  public void generateProlog(DdlWrite write) throws IOException {
    super.generateProlog(write);

    generateTVPDefinitions(write, "ebean_timestamp_tvp", "timestamp");
    generateTVPDefinitions(write, "ebean_date_tvp", "date");
    generateTVPDefinitions(write, "ebean_number_tvp", "number(38)");
    generateTVPDefinitions(write, "ebean_float_tvp", "number(19,4)");
    generateTVPDefinitions(write, "ebean_string_tvp", "varchar2(32767)");
    generateTVPDefinitions(write, "ebean_binary_tvp", "raw(32767)"); // for binary-UUIDs
    // TODO: UUID

  }

  private void generateTVPDefinitions(DdlWrite write, String name, String definition) throws IOException {
    name = name.toUpperCase();
    dropTVP(write.dropAll(), name);
    createTVP(write.apply(), name, definition);
  }

  private void dropTVP(DdlBuffer ddl, String name) throws IOException {
    ddl.append("drop type ").append(name).endOfStatement();
  }

  private void createTVP(DdlBuffer ddl, String name, String definition) throws IOException {
    ddl.append("delimiter $$\ncreate or replace type ").append(name).append(" is table of ")
    .append(definition).endOfStatement();
    ddl.append("/\n$$\n");
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
