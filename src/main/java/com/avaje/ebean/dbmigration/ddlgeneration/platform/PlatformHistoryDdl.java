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
  void configure(ServerConfig serverConfig, PlatformDdl platformDdl);

  /**
   * Add history support to the table using platform specific mechanism.
   */
  void createWithHistory(DdlWrite writer, MTable table) throws IOException;

  /**
   * Drop history support for the given table.
   */
  void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable) throws IOException;

  /**
   * Add history support to the given table.
   */
  void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable) throws IOException;

  /**
   * Regenerate the history triggers/stored function due to column added/dropped/included or excluded.
   */
  void regenerateHistoryTriggers(DdlWrite write, HistoryTableUpdate baseTable) throws IOException;
}
