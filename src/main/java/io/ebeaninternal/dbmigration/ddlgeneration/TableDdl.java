package io.ebeaninternal.dbmigration.ddlgeneration;

import io.ebeaninternal.dbmigration.migration.AddColumn;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.AddTableComment;
import io.ebeaninternal.dbmigration.migration.AddUniqueConstraint;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.AlterForeignKey;
import io.ebeaninternal.dbmigration.migration.CreateIndex;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropIndex;
import io.ebeaninternal.dbmigration.migration.DropTable;

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
   */
  void generate(DdlWrite writer, AlterForeignKey alterForeignKey) throws IOException;

  /**
   * Generate any extra DDL such as stored procedures or TableValueParameters.
   */
  void generateProlog(DdlWrite write) throws IOException;

  /**
   * Generate any extra DDL such as regeneration of history triggers.
   */
  void generateEpilog(DdlWrite write) throws IOException;
}
