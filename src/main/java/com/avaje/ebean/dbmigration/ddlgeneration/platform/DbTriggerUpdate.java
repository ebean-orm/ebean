package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.dbmigration.ddlgeneration.DdlBuffer;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;

import java.util.List;

/**
 * DB trigger update when a change occurs on a table with history.
 */
public class DbTriggerUpdate {

  private final String baseTableName;

  private final String historyTableName;

  private final DdlWrite writer;

  private DdlWrite.Mode mode;

  private List<String> includedColumns;

  public DbTriggerUpdate(String baseTableName, String historyTableName, DdlWrite writer) {
    this.baseTableName = baseTableName;
    this.historyTableName = historyTableName;
    this.writer = writer;
  }

  /**
   * Prepare for use given the mode and columns included in history.
   */
  public void prepare(DdlWrite.Mode mode, List<String> includedColumns) {
    this.mode = mode;
    this.includedColumns = includedColumns;
  }

  /**
   * Return the appropriate buffer for the current mode.
   */
  public DdlBuffer historyBuffer() {
    return writer.historyBuffer(mode);
  }

  /**
   * Return the appropriate drop dependency buffer for the current mode.
   */
  public DdlBuffer dropDependencyBuffer() {
    return writer.dropDependencies(mode);
  }

  /**
   * Return the base table name.
   */
  public String getBaseTable() {
    return baseTableName;
  }

  /**
   * Return the history table name.
   */
  public String getHistoryTable() {
    return historyTableName;
  }

  /**
   * Return the included columns.
   */
  public List<String> getColumns() {
    return includedColumns;
  }

}
