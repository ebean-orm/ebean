package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.AddHistoryTable;
import com.avaje.ebean.dbmigration.migration.DropHistoryTable;
import com.avaje.ebean.dbmigration.model.MTable;

import java.io.IOException;

/**
 * Defines the implementation for adding history support to a table.
 */
public interface PlatformHistoryDdl {

  /**
   * Configure typically reading the
   */
  void configure(ServerConfig serverConfig);

  /**
   * Add history support to the table using platform specific mechanism.
   */
  void createWithHistory(DdlWrite writer, MTable table) throws IOException;

  void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException;

  void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException;

  void regenerateHistoryTriggers(DdlWrite write, String baseTable) throws IOException;
}
