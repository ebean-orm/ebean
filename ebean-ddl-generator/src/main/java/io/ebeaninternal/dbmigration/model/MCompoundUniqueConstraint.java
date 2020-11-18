package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.ddlgeneration.platform.DdlHelp;
import io.ebeaninternal.dbmigration.migration.AddUniqueConstraint;
import io.ebeaninternal.dbmigration.migration.UniqueConstraint;

import java.util.Arrays;
import java.util.Objects;

import static io.ebeaninternal.dbmigration.ddlgeneration.platform.SplitColumns.split;
import static io.ebeaninternal.dbmigration.ddlgeneration.platform.SplitColumns.splitWithNull;

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

  private final String platforms;

  private String[] nullableColumns;

  /**
   * Create for OneToOne.
   */
  public MCompoundUniqueConstraint(String[] columns, String name) {
    this.name = name;
    this.columns = columns;
    this.oneToOne = true;
    this.platforms = null;
  }

  public MCompoundUniqueConstraint(String[] columns, boolean oneToOne, String name, String platforms) {
    this.name = name;
    this.columns = columns;
    this.oneToOne = oneToOne;
    this.platforms = platforms;
  }

  public MCompoundUniqueConstraint(AddUniqueConstraint change) {
    this.name = change.getConstraintName();
    this.columns = split(change.getColumnNames());
    this.oneToOne = Boolean.TRUE.equals(change.isOneToOne());
    this.platforms = change.getPlatforms();
    this.nullableColumns = splitWithNull(change.getNullableColumns());
  }

  public MCompoundUniqueConstraint(UniqueConstraint uq) {
    this.name = uq.getName();
    this.columns = split(uq.getColumnNames());
    this.oneToOne = Boolean.TRUE.equals(uq.isOneToOne());
    this.platforms = uq.getPlatforms();
    this.nullableColumns = splitWithNull(uq.getNullableColumns());
  }

  /**
   * Return the columns for this unique constraint.
   */
  public String[] getColumns() {
    return columns;
  }

  /**
   * Return true if this unique constraint is specifically for OneToOne mapping.
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

  public String getPlatforms() {
    return platforms;
  }

  public UniqueConstraint getUniqueConstraint() {
    UniqueConstraint uq = new UniqueConstraint();
    uq.setName(getName());
    uq.setColumnNames(join(columns));
    uq.setNullableColumns(join(nullableColumns));
    uq.setOneToOne(isOneToOne());
    uq.setPlatforms(platforms);
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
    create.setPlatforms(platforms);
    return create;
  }

  /**
   * Create a AddUniqueConstraint migration with 'DROP CONSTRAINT' set for this index.
   */
  public AddUniqueConstraint dropUniqueConstraint(String tableName) {
    AddUniqueConstraint drop = new AddUniqueConstraint();
    drop.setConstraintName(name);
    drop.setTableName(tableName);
    drop.setColumnNames(DdlHelp.DROP_CONSTRAINT);
    drop.setNullableColumns(join(nullableColumns));
    drop.setPlatforms(platforms);
    return drop;
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
    // not including platforms in equals check
    return oneToOne == other.oneToOne
      && Objects.equals(name, other.name)
      && Arrays.equals(columns, other.columns)
      && Arrays.equals(nullableColumns, other.nullableColumns);
  }
}
