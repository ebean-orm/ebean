package com.avaje.ebean.dbmigration.ddlgeneration;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import com.avaje.ebean.dbmigration.migration.DropColumn;

import java.io.IOException;

/**
 */
public interface DdlHandler {

  void generate(DdlWrite writer, ChangeSet changeSet) throws IOException;

  void generate(DdlWrite writer, CreateTable createTable) throws IOException;

  void generate(DdlWrite writer, AddColumn addColumn) throws IOException;

  void generate(DdlWrite writer, DropColumn dropColumn) throws IOException;

  void generate(DdlWrite writer, AlterColumn alterColumn) throws IOException;

}