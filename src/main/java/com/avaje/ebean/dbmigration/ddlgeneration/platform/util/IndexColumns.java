package com.avaje.ebean.dbmigration.ddlgeneration.platform.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of columns making up a particular index (column order is important).
 */
public class IndexColumns {

  List<String> columns = new ArrayList<String>(4);

  /**
   * Construct representing as a single column index.
   */
  public IndexColumns(String column) {
    columns.add(column);
  }

  /**
   * Construct representing index.
   */
  public IndexColumns(String[] columnNames) {
    for (int i = 0; i < columnNames.length; i++) {
      columns.add(columnNames[i]);
    }
  }

  /**
   * Return true if this index matches (same single column).
   */
  public boolean isMatch(String singleColumn) {
    return columns.size() == 1 && columns.get(0).equals(singleColumn);
  }

  /**
   * Return true if this index matches (same single column).
   */
  public boolean isMatch(List<String> columnNames) {
    if (columns.size() != columnNames.size()) {
      return false;
    }
    for (int i = 0; i <columns.size() ; i++) {
      if (!columns.get(i).equals(columnNames.get(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return true if this index matches (same columns same order).
   */
  public boolean isMatch(IndexColumns other) {
    return columns.equals(other.columns);
  }

  /**
   * Add a unique index based on the single column.
   */
  protected void add(String column) {
    columns.add(column);
  }

}
