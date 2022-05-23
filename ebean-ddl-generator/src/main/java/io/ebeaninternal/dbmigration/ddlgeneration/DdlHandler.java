package io.ebeaninternal.dbmigration.ddlgeneration;

import io.ebeaninternal.dbmigration.migration.*;

/**
 * DDL generation interface.
 */
public interface DdlHandler {

  void generate(DdlWrite writer, ChangeSet changeSet);

  void generate(DdlWrite writer, CreateSchema createSchema);

  void generate(DdlWrite writer, CreateTable createTable);

  void generate(DdlWrite writer, DropTable dropTable);

  void generate(DdlWrite writer, AlterTable dropTable);

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
