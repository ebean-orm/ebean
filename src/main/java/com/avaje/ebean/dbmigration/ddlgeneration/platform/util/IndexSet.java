package com.avaje.ebean.dbmigration.ddlgeneration.platform.util;

import java.util.ArrayList;
import java.util.List;

/**
 * The indexes held on the table.
 * <p>
 * Used to detect when we don't need to add an index on the foreign key columns
 * when there is an existing unique constraint with the same columns.
 */
public class IndexSet {

  private List<IndexColumns> indexes = new ArrayList<IndexColumns>();

  /**
   * Clear the indexes (for each table).
   */
  public void clear() {
    indexes.clear();
  }

  /**
   * Add an index for the given column.
   */
  public void add(String column) {
    indexes.add(new IndexColumns(column));
  }

  /**
   * Return true if an index should be added for the given columns.
   * <p>
   * Returning false indicates there is an existing index (unique constraint) with these columns
   * and that an extra index should not be added.
   * </p>
   */
  public boolean add(String[] columns) {
    IndexColumns newIndex = new IndexColumns(columns);
    for (int i = 0; i < indexes.size(); i++) {
      if (indexes.get(i).isMatch(newIndex)) {
        return false;
      }
    }
    indexes.add(newIndex);
    return true;
  }

  /**
   * Add the externally created unique constraint here so that we check later if foreign key indexes
   * don't need to be created (as the columns match this unique constraint).
   */
  public void add(IndexColumns index) {
    indexes.add(index);
  }

  public boolean contains(String column) {

    for (IndexColumns index : indexes) {
      if (index.isMatch(column)) {
        return true;
      }
    }
    return false;
  }

  public boolean contains(List<String> columns) {

    for (IndexColumns index : indexes) {
      if (index.isMatch(columns)) {
        return true;
      }
    }
    return false;
  }

  public List<IndexColumns> getIndexes() {
    return indexes;
  }

}
