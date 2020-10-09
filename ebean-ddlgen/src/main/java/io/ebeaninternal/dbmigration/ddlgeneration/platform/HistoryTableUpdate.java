package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean holding comments relating to a history table that needs to have it's
 * associated triggers/function updated due to columns added/dropped/included
 * or excluded.
 */
public class HistoryTableUpdate {

  /**
   * Column change type.
   */
  public enum Change {
    ADD,
    DROP,
    INCLUDE,
    EXCLUDE,
    ALTER
  }

  private static class Column {

    final Change change;

    final String column;

    public Column(Change change, String column) {
      this.change = change;
      this.column = column;
    }

    @Override
    public String toString() {
      return description();
    }

    public String description() {
      return change.name().toLowerCase() + " " + column;
    }

  }

  private final String baseTable;

  private final List<Column> columnChanges = new ArrayList<>();

  /**
   * Construct with a given base table name.
   */
  public HistoryTableUpdate(String baseTable) {
    this.baseTable = baseTable;
  }

  /**
   * Return a description of the changes that cause the history trigger/function
   * to be regenerated (added, included, excluded and dropped columns).
   */
  public String description() {
    return columnChanges.toString();
  }

  /**
   * Add a comment for column added, dropped, included or excluded.
   */
  public void add(Change change, String column) {
    columnChanges.add(new Column(change, column));
  }

  /**
   * Return the base table name.
   */
  public String getBaseTable() {
    return baseTable;
  }

}
