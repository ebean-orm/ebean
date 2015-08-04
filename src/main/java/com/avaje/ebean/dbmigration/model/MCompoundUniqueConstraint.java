package com.avaje.ebean.dbmigration.model;

/**
 * A unique constraint for multiple columns.
 * <p>
 *   Note that unique constraint on a single column is instead
 *   a boolean flag on the associated MColumn.
 * </p>
 */
public class MCompoundUniqueConstraint {

  /**
   * The columns combined to be unique.
   */
  private final String[] columns;

  public MCompoundUniqueConstraint(String[] columns) {
    this.columns = columns;
  }

  public String[] getColumns() {
    return columns;
  }
}
