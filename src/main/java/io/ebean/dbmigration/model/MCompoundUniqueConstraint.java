package io.ebean.dbmigration.model;

import java.util.Arrays;
import java.util.Objects;

import io.ebean.dbmigration.migration.CreateUniqueConstraint;
import io.ebean.dbmigration.migration.DropUniqueConstraint;

/**
 * A unique constraint for multiple columns.
 * <p>
 * Note that unique constraint on a single column is instead
 * a boolean flag on the associated MColumn.
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
  
  /**
   * Return a CreateUniqueConstraint migration for this constraint.
   */
  public CreateUniqueConstraint createUniqueConstraint(String tableName) {
    CreateUniqueConstraint create = new CreateUniqueConstraint();
    create.setConstraintName(name);
    create.setTableName(tableName);
    create.setColumnNames(join());
    return create;
  }

  /**
   * Create a DropIndex migration for this index.
   */
  public DropUniqueConstraint dropUniqueConstraint(String tableName) {
    DropUniqueConstraint dropUniqueConstraint = new DropUniqueConstraint();
    dropUniqueConstraint.setConstraintName(name);
    dropUniqueConstraint.setTableName(tableName);
    return dropUniqueConstraint;
  }

  private String join() {
    StringBuilder sb = new StringBuilder(50);
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(columns[i]);
    }
    return sb.toString();
  }
  
  @Override
  public int hashCode() {
    return Arrays.hashCode(columns) + 31 * Objects.hash(name, oneToOne);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof MCompoundUniqueConstraint)) {
      return false;
    }
    MCompoundUniqueConstraint other = (MCompoundUniqueConstraint) obj;
    return Arrays.equals(columns, other.columns)
        && Objects.equals(name, other.name)
        && oneToOne == other.oneToOne;
  }
}
