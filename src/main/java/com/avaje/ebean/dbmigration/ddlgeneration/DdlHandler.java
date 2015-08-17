package com.avaje.ebean.dbmigration.ddlgeneration;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.AddHistoryTable;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.CreateIndex;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import com.avaje.ebean.dbmigration.migration.DropColumn;
import com.avaje.ebean.dbmigration.migration.DropHistoryTable;
import com.avaje.ebean.dbmigration.migration.DropIndex;
import com.avaje.ebean.dbmigration.migration.DropTable;

import java.io.IOException;

/**
 */
public interface DdlHandler {

  void generate(DdlWrite writer, ChangeSet changeSet) throws IOException;

  void generate(DdlWrite writer, CreateTable createTable) throws IOException;

  void generate(DdlWrite writer, DropTable dropTable) throws IOException;

  void generate(DdlWrite writer, AddColumn addColumn) throws IOException;

  void generate(DdlWrite writer, DropColumn dropColumn) throws IOException;

  void generate(DdlWrite writer, AlterColumn alterColumn) throws IOException;

  void generate(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException;

  void generate(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException;

  void generate(DdlWrite writer, CreateIndex createIndex) throws IOException;

  void generate(DdlWrite writer, DropIndex dropIndex) throws IOException;

  void generateExtra(DdlWrite write) throws IOException;
}