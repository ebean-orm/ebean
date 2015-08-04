package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.Configuration;
import com.avaje.ebean.dbmigration.migration.DefaultTablespace;

/**
 * Holds configuration such as the default tablespaces to use for tables,
 * indexes, history tables etc.
 */
public class MConfiguration {

  /**
   * Default tablespace for tables.
   */
  protected String tableTablespace;

  /**
   * Default tablespace for indexes.
   */
  protected String indexTablespace;

  /**
   * Default tablespace for history tables.
   */
  protected String historyTablespace;

  /**
   * Apply the migration configuration.
   * <p>
   *   It is expected that these are applied in the correct chronological order
   *   from earliest to latest.
   * </p>
   */
  public void apply(Configuration configuration) {

    DefaultTablespace defaultTablespace = configuration.getDefaultTablespace();
    if (defaultTablespace != null) {
      String tables = defaultTablespace.getTables();
      if (isNotEmpty(tables)) {
        this.tableTablespace = tables;
      }
      String indexes = defaultTablespace.getIndexes();
      if (isNotEmpty(indexes)) {
        this.indexTablespace = indexes;
      }
      String history = defaultTablespace.getHistory();
      if (isNotEmpty(history)) {
        this.historyTablespace = history;
      }
    }
  }

  /**
   * Return the default tablespace to use for tables.
   */
  public String getTableTablespace() {
    return tableTablespace;
  }

  /**
   * Return the default tablespace to use for indexes.
   */
  public String getIndexTablespace() {
    return indexTablespace;
  }

  /**
   * Return the default tablespace to use for history tables.
   */
  public String getHistoryTablespace() {
    return historyTablespace;
  }

  protected boolean isNotEmpty(String tables) {
    return tables != null && !tables.trim().isEmpty();
  }
}
