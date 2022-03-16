package io.ebeaninternal.dbmigration.ddlgeneration;

import io.ebeaninternal.dbmigration.migration.AddColumn;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.AddTableComment;
import io.ebeaninternal.dbmigration.migration.AddUniqueConstraint;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.AlterForeignKey;
import io.ebeaninternal.dbmigration.migration.AlterTable;
import io.ebeaninternal.dbmigration.migration.CreateIndex;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropIndex;
import io.ebeaninternal.dbmigration.migration.DropTable;

/**
 * Write table DDL.
 */
public interface TableDdl {

  /**
   * Generate the create table change.
   */
  void generate(DdlWrite writer, CreateTable createTable);

  /**
   * Write the drop column change.
   */
  void generate(DdlWrite writer, DropTable dropTable);
  
  /**
   * Write alter table changes.
   */
  void generate(DdlWrite writer, AlterTable dropTable);

  /**
   * Write the add column change.
   */
  void generate(DdlWrite writer, AddColumn addColumn);

  /**
   * Write the alter column changes.
   */
  void generate(DdlWrite writer, AlterColumn alterColumn);

  /**
   * Write the drop column change.
   */
  void generate(DdlWrite writer, DropColumn dropColumn);

  /**
   * Write the AddTableComment change.
   */
  void generate(DdlWrite writer, AddTableComment addTableComment);

  /**
   * Write the AddHistoryTable change.
   */
  void generate(DdlWrite writer, AddHistoryTable addHistoryTable);

  /**
   * Write the DropHistoryTable change.
   */
  void generate(DdlWrite writer, DropHistoryTable dropHistoryTable);

  /**
   * Generate the create index change.
   */
  void generate(DdlWrite writer, CreateIndex createIndex);

  /**
   * Write the drop index change.
   */
  void generate(DdlWrite writer, DropIndex dropIndex);

  /**
   * Write add unique constraint.
   */
  void generate(DdlWrite writer, AddUniqueConstraint constraint);

  /**
   * Writes alter foreign key statements.
   */
  void generate(DdlWrite writer, AlterForeignKey alterForeignKey);

  /**
   * Generate any extra DDL such as stored procedures or TableValueParameters.
   */
  void generateProlog(DdlWrite writer);

  /**
   * Generate any extra DDL such as regeneration of history triggers.
   */
  void generateEpilog(DdlWrite writer);
}
