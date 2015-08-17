package com.avaje.ebean.dbmigration.ddlgeneration.platform;

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
    EXCLUDE
  }

  public static class Column {

    public final Change change;
    public final String column;
    public Column(Change change, String column) {
      this.change = change;
      this.column = column;
    }

    public String description() {
      return change.name().toLowerCase()+" "+column;
    }

    public void apply(List<String> includedColumns) {
      switch (change) {
        case ADD:
        case INCLUDE: {
          includedColumns.remove(column);
          break;
        }
        case DROP:
        case EXCLUDE: {
          includedColumns.add(column);
          break;
        }
        default:
          throw new IllegalStateException("Unexpected change "+change);
      }
    }
  }

  private final String baseTable;

  private final List<Column> columnChanges = new ArrayList<Column>();

  /**
   * Construct with a given base table name.
   */
  public HistoryTableUpdate(String baseTable) {
    this.baseTable = baseTable;
  }

  /**
   * Return a description of the changes that cause the history trigger/function
   * to be regenerated (added, dropped, included or excluded columns).
   */
  public String description() {
    StringBuilder sb = new StringBuilder(90);
    for (int i = 0; i < columnChanges.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(columnChanges.get(i).description());
    }
    return sb.toString();
  }

  public void toRevertedColumns(List<String> includedColumns) {

    for (Column columnChange : columnChanges) {
      columnChange.apply(includedColumns);
    }
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

  /**
   * Return the comments.
   */
  public List<Column> getColumnChanges() {
    return columnChanges;
  }
}
