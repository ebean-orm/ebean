package io.ebean.dbmigration.ddlgeneration.platform;

import java.io.IOException;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.dbmigration.ddlgeneration.DdlBuffer;
import io.ebean.dbmigration.ddlgeneration.DdlWrite;

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
  public void generatePreamble(DdlWrite write) throws IOException {
    super.generatePreamble(write);

    generateTVPDefinitions(write, "EBEAN_TIMESTAMP_TVP", "timestamp");
    generateTVPDefinitions(write, "EBEAN_DATE_TVP", "date");
    generateTVPDefinitions(write, "EBEAN_NUMBER_TVP", "number(38)");
    generateTVPDefinitions(write, "EBEAN_FLOAT_TVP", "number(19,4)");
    generateTVPDefinitions(write, "EBEAN_STRING_TVP", "varchar2(32767)");

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
}
