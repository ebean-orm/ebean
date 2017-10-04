package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.config.ServerConfig;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.model.MTable;

import java.io.IOException;

/**
 * Defines the implementation for adding history support to a table.
 */
public interface PlatformHistoryDdl {

  /**
   * Configure typically reading the necessary parameters from ServerConfig and Platform.
   */
  void configure(ServerConfig serverConfig, PlatformDdl platformDdl);

  /**
   * Creates a new table and add history support to the table using platform specific mechanism.
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
  void updateTriggers(DdlWrite write, HistoryTableUpdate baseTable) throws IOException;
}
