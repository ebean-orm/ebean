package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.ddlgeneration.platform.DdlHelp;
import io.ebeaninternal.dbmigration.migration.AddUniqueConstraint;
import io.ebeaninternal.dbmigration.migration.UniqueConstraint;

import java.util.Arrays;
import java.util.Objects;

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

  private String[] nullableColumns;

  public MCompoundUniqueConstraint(String[] columns, Boolean oneToOne, String name) {
    this.name = name;
    this.columns = columns;
    this.oneToOne = Boolean.TRUE.equals(oneToOne);
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

  public UniqueConstraint getUniqueConstraint() {
    UniqueConstraint uq = new UniqueConstraint();
    uq.setName(getName());
    uq.setColumnNames(join(columns));
    uq.setNullableColumns(join(nullableColumns));
    uq.setOneToOne(isOneToOne());
    return uq;
  }

  /**
   * Return a AddUniqueConstraint migration for this constraint.
   */
  public AddUniqueConstraint addUniqueConstraint(String tableName) {
    AddUniqueConstraint create = new AddUniqueConstraint();
    create.setConstraintName(getName());
    create.setTableName(tableName);
    create.setColumnNames(join(columns));
    create.setNullableColumns(join(nullableColumns));
    create.setOneToOne(isOneToOne());
    return create;
  }

  /**
   * Create a AddUniqueConstraint migration with 'DROP CONSTRAINT' set for this index.
   */
  public AddUniqueConstraint dropUniqueConstraint(String tableName) {
    AddUniqueConstraint dropUniqueConstraint = new AddUniqueConstraint();
    dropUniqueConstraint.setConstraintName(name);
    dropUniqueConstraint.setTableName(tableName);
    dropUniqueConstraint.setColumnNames(DdlHelp.DROP_CONSTRAINT);
    dropUniqueConstraint.setNullableColumns(join(nullableColumns));
    return dropUniqueConstraint;
  }

  public void setNullableColumns(String[] nullableColumns) {
    if (nullableColumns != null && nullableColumns.length == 0) {
      this.nullableColumns = null;
    } else {
      this.nullableColumns = nullableColumns;
    }
  }

  private String join(String[] arr) {
    if (arr == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder(50);
    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(arr[i]);
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
      && Arrays.equals(nullableColumns, other.nullableColumns)
      && Objects.equals(name, other.name)
      && oneToOne == other.oneToOne;
  }
}
