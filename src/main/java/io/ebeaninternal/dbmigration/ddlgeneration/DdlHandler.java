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

import java.io.IOException;

/**
 * DDL generation interface.
 */
public interface DdlHandler {

  void generate(DdlWrite writer, ChangeSet changeSet) throws IOException;

  void generate(DdlWrite writer, CreateTable createTable) throws IOException;

  void generate(DdlWrite writer, DropTable dropTable) throws IOException;

  void generate(DdlWrite writer, AddTableComment addTableComment) throws IOException;

  void generate(DdlWrite writer, AddColumn addColumn) throws IOException;

  void generate(DdlWrite writer, DropColumn dropColumn) throws IOException;

  void generate(DdlWrite writer, AlterColumn alterColumn) throws IOException;

  void generate(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException;

  void generate(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException;

  void generate(DdlWrite writer, CreateIndex createIndex) throws IOException;

  void generate(DdlWrite writer, DropIndex dropIndex) throws IOException;

  void generate(DdlWrite writer, AddUniqueConstraint constraint) throws IOException;

  void generate(DdlWrite writer, AlterForeignKey alterForeignKey) throws IOException;

  void generateProlog(DdlWrite write) throws IOException;

  void generateEpilog(DdlWrite write) throws IOException;
}
