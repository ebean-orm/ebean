package com.avaje.ebean.dbmigration.ddlgeneration;

import com.avaje.ebean.dbmigration.migration.*;

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

  void generateExtra(DdlWrite write) throws IOException;
}
