package io.ebeaninternal.dbmigration.ddlgeneration;

import io.ebeaninternal.dbmigration.migration.AddColumn;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.AddTableComment;
import io.ebeaninternal.dbmigration.migration.AddUniqueConstraint;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.AlterForeignKey;
import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.migration.CreateIndex;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropIndex;
import io.ebeaninternal.dbmigration.migration.DropTable;

/**
 * DDL generation interface.
 */
public interface DdlHandler {

  void generate(DdlWrite writer, ChangeSet changeSet);

  void generate(DdlWrite writer, CreateTable createTable);

  void generate(DdlWrite writer, DropTable dropTable);

  void generate(DdlWrite writer, AddTableComment addTableComment);

  void generate(DdlWrite writer, AddColumn addColumn);

  void generate(DdlWrite writer, DropColumn dropColumn);

  void generate(DdlWrite writer, AlterColumn alterColumn);

  void generate(DdlWrite writer, AddHistoryTable addHistoryTable);

  void generate(DdlWrite writer, DropHistoryTable dropHistoryTable);

  void generate(DdlWrite writer, CreateIndex createIndex);

  void generate(DdlWrite writer, DropIndex dropIndex);

  void generate(DdlWrite writer, AddUniqueConstraint constraint);

  void generate(DdlWrite writer, AlterForeignKey alterForeignKey);

  void generateProlog(DdlWrite writer);

  void generateEpilog(DdlWrite writer);
}
