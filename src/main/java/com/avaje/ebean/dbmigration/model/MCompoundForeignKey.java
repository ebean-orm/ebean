package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.Column;
import com.avaje.ebean.dbmigration.migration.ForeignKey;

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

  public ForeignKey createForeignKey() {
    ForeignKey fk = new ForeignKey();
    fk.setColumnNames(toColumnNames(columns));
    fk.setRefColumnNames(toColumnNames(referenceColumns));
    fk.setRefTableName(referenceTable);
    return fk;
  }

  /**
   * Return as an array of string column names.
   */
  private String toColumnNames(List<String> columns) {

    StringBuilder sb = new StringBuilder(40);
    for (int i = 0; i < columns.size(); i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(columns.get(i));
    }
    return sb.toString();
  }

}
