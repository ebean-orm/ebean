package com.avaje.ebean.dbmigration.ddlgeneration;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.AddHistoryTable;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.CreateIndex;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import com.avaje.ebean.dbmigration.migration.DropColumn;
import com.avaje.ebean.dbmigration.migration.DropHistoryTable;
import com.avaje.ebean.dbmigration.migration.DropIndex;
import com.avaje.ebean.dbmigration.migration.DropTable;

import java.io.IOException;

/**
 * Write table DDL.
 */
public interface TableDdl {

  /**
   * Generate the create table change.
   */
  void generate(DdlWrite writer, CreateTable createTable) throws IOException;

  /**
   * Write the drop column change.
   */
  void generate(DdlWrite writer, DropTable dropTable) throws IOException;

  /**
   * Write the add column change.
   */
  void generate(DdlWrite writer, AddColumn addColumn) throws IOException;

  /**
   * Write the alter column changes.
   */
  void generate(DdlWrite writer, AlterColumn alterColumn) throws IOException;

  /**
   * Write the drop column change.
   */
  void generate(DdlWrite writer, DropColumn dropColumn) throws IOException;

  /**
   * Write the AddHistoryTable change.
   */
  void generate(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException;

  /**
   * Write the DropHistoryTable change.
   */
  void generate(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException;

  /**
   * Generate the create index change.
   */
  void generate(DdlWrite writer, CreateIndex createIndex) throws IOException;

  /**
   * Write the drop index change.
   */
  void generate(DdlWrite writer, DropIndex dropIndex) throws IOException;

  /**
   * Generate any extra DDL such as regeneration of history triggers.
   */
  void generateExtra(DdlWrite write) throws IOException;
}
