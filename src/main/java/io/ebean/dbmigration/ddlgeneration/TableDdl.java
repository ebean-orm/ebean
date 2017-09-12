package io.ebean.dbmigration.ddlgeneration;

import io.ebean.dbmigration.migration.AddColumn;
import io.ebean.dbmigration.migration.AddHistoryTable;
import io.ebean.dbmigration.migration.AddTableComment;
import io.ebean.dbmigration.migration.AddUniqueConstraint;
import io.ebean.dbmigration.migration.AlterColumn;
import io.ebean.dbmigration.migration.AlterForeignKey;
import io.ebean.dbmigration.migration.CreateIndex;
import io.ebean.dbmigration.migration.CreateTable;
import io.ebean.dbmigration.migration.DropColumn;
import io.ebean.dbmigration.migration.DropHistoryTable;
import io.ebean.dbmigration.migration.DropIndex;
import io.ebean.dbmigration.migration.DropTable;

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
   * Write the AddTableComment change.
   */
  void generate(DdlWrite writer, AddTableComment addTableComment) throws IOException;

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
   * Write add unique constraint.
   */
  void generate(DdlWrite writer, AddUniqueConstraint constraint) throws IOException;

  /**
   * Writes alter foreign key statements.
   * @throws IOException 
   */
  void generate(DdlWrite writer, AlterForeignKey alterForeignKey) throws IOException;

  /**
   * Generate any extra DDL such as regeneration of history triggers.
   */
  void generatePreamble(DdlWrite write) throws IOException;
  
  /**
   * Generate any extra DDL such as regeneration of history triggers.
   */
  void generateExtra(DdlWrite write) throws IOException;

  
}
