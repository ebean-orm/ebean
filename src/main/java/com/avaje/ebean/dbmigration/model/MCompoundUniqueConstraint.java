package com.avaje.ebean.dbmigration.model;

/**
 * A unique constraint for multiple columns.
 * <p>
 *   Note that unique constraint on a single column is instead
 *   a boolean flag on the associated MColumn.
 * </p>
 */
public class MCompoundUniqueConstraint {

  private final String name;

  /**
   * Flag if true indicates this was specifically created for a OneToOne mapping.
   */
  private final boolean oneToOne;

  /**
   * The columns combined to be unique.
   */
  private final String[] columns;

  public MCompoundUniqueConstraint(String[] columns, boolean oneToOne, String name) {
    this.name = name;
    this.columns = columns;
    this.oneToOne = oneToOne;
  }

  /**
   * Return the columns for this unique constraint.
   */
  public String[] getColumns() {
    return columns;
  }

  /**
   * Return true if this unqiue constraint is specifically for OneToOne mapping.
   */
  public boolean isOneToOne() {
    return oneToOne;
  }

  /**
   * Return the constraint name.
   */
  public String getName() {
    return name;
  }
}
