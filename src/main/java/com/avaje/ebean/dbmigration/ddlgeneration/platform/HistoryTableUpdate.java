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

  private static class Column {

    final Change change;

    final String column;

    public Column(Change change, String column) {
      this.change = change;
      this.column = column;
    }

    public String description() {
      return change.name().toLowerCase()+" "+column;
    }

    private boolean isChangeFor(boolean apply) {
      return apply ? change != Change.DROP : change == Change.DROP;
    }

    private void revert(List<String> includedColumns) {
      switch (change) {
        case ADD:
        case INCLUDE: {
          includedColumns.remove(column);
          break;
        }
        case EXCLUDE: {
          includedColumns.add(column);
          break;
        }
        case DROP:
          break;
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

  private boolean isChangeFor(boolean apply) {
    for (Column columnChange : columnChanges) {
      if (columnChange.isChangeFor(apply)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return true if the change includes apply changes (ADD, INCLUDE, EXCLUDE).
   */
  public boolean hasApplyChanges() {
    return isChangeFor(true);
  }

  /**
   * Return true if the change includes DROP column.
   */
  public boolean hasDropChanges() {
    return isChangeFor(false);
  }

  /**
   * Return a description of the changes that cause the history trigger/function
   * to be regenerated (added, included or excluded columns).
   */
  public String descriptionForApply() {
    return descriptionFor(true);
  }

  /**
   * Return a description of the changes that cause the history trigger/function
   * to be regenerated in the drop script (dropped columns only).
   */
  public String descriptionForDrop() {
    return descriptionFor(false);
  }

  private String descriptionFor(boolean apply) {

    StringBuilder sb = new StringBuilder(90);
    boolean first = true;
    for (Column column : columnChanges) {
      if (column.isChangeFor(apply)) {
        if (first) {
          first = false;
        } else {
          sb.append(", ");
        }
        sb.append(column.description());
      }
    }
    return sb.toString();
  }

  /**
   * Reverse the apply changes which equates to removing any newly added or
   * included columns.
   */
  public void toRevertedColumns(List<String> includedColumns) {

    for (Column columnChange : columnChanges) {
      columnChange.revert(includedColumns);
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

}
