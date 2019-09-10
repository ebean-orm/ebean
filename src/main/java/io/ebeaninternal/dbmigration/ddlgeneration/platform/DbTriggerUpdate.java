package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebeaninternal.dbmigration.ddlgeneration.DdlBuffer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;

import java.util.List;

/**
 * DB trigger update when a change occurs on a table with history.
 */
public class DbTriggerUpdate {

  private final String baseTableName;

  private final String historyTableName;

  private final DdlWrite writer;

  private final List<String> columns;

  public DbTriggerUpdate(String baseTableName, String historyTableName, DdlWrite writer, List<String> columns) {
    this.baseTableName = baseTableName;
    this.historyTableName = historyTableName;
    this.writer = writer;
    this.columns = columns;
  }

  /**
   * Return the appropriate buffer for the current mode.
   */
  public DdlBuffer historyViewBuffer() {
    return writer.applyHistoryView();
  }

  /**
   * Return the appropriate buffer for the current mode.
   */
  public DdlBuffer historyTriggerBuffer() {
    return writer.applyHistoryTrigger();
  }


  /**
   * Return the appropriate drop dependency buffer for the current mode.
   */
  public DdlBuffer dropDependencyBuffer() {
    return writer.applyDropDependencies();
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
    return columns;
  }

}
