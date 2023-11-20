package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DatabaseBuilder;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.model.MTable;

/**
 * Defines the implementation for adding history support to a table.
 */
public interface PlatformHistoryDdl {

  /**
   * Configure typically reading the necessary parameters from DatabaseConfig and Platform.
   */
  void configure(DatabaseBuilder.Settings config, PlatformDdl platformDdl);

  /**
   * Creates a new table and add history support to the table using platform specific mechanism.
   */
  void createWithHistory(DdlWrite writer, MTable table);

  /**
   * Drop history support for the given table.
   */
  void dropHistoryTable(DdlWrite writer, DropHistoryTable dropHistoryTable);

  /**
   * Add history support to the given table.
   */
  void addHistoryTable(DdlWrite writer, AddHistoryTable addHistoryTable);

  /**
   * Regenerate the history triggers/stored function due to column added/dropped/included or excluded.
   *
   * Note: This function may be called multiple times for the same table.
   */
  default void updateTriggers(DdlWrite writer, String tableName) {
    // nop
  }

  /**
   * When history is table based, then alters on the live tables are applied also to the history tables. This is required for
   * DbTriggerBased histories or on platforms like Hana, which are not SQL2011 history compatible (at least from DDL perspective)
   */
  interface TableBased extends PlatformHistoryDdl {
    /**
     * Returns the history table name with propert quotes.
     */
    public String historyTableName(String baseTableName);
  }
}
