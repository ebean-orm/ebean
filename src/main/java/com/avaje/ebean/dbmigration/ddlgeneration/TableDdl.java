package com.avaje.ebean.dbmigration.ddlgeneration;

import com.avaje.ebean.dbmigration.migration.CreateTable;

import java.io.IOException;

/**
 * Write table DDL.
 */
public interface TableDdl {

  /**
   * Generate the create table DDL.
   */
  void generate(DdlWrite writer, CreateTable createTable) throws IOException;
}
