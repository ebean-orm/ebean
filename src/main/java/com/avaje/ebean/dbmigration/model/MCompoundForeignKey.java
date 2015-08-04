package com.avaje.ebean.dbmigration.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A unique constraint for multiple columns.
 * <p>
 *   Note that unique constraint on a single column is instead
 *   a boolean flag on the associated MColumn.
 * </p>
 */
public class MCompoundForeignKey {

  private final String referenceTable;
  private final List<String> columns = new ArrayList<String>();
  private final List<String> referenceColumns = new ArrayList<String>();

  public MCompoundForeignKey(String referenceTable) {
    this.referenceTable = referenceTable;
  }

  public void addColumnPair(String dbCol, String refColumn) {
    columns.add(dbCol);
    referenceColumns.add(refColumn);
  }
}
