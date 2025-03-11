package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.core.InternString;
import io.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;

/**
 * A join pair of local and foreign properties.
 */
public final class TableJoinColumn {

  /**
   * The local database column name.
   */
  private final String localDbColumn;
  private final String localSqlFormula;
  /**
   * The foreign database column name.
   */
  private final String foreignDbColumn;
  private final String foreignSqlFormula;
  private final boolean insertable;
  private final boolean updateable;
  private final boolean nullable;
  /**
   * Hash for including in a query plan
   */
  private final int queryHash;

  /**
   * Create the pair.
   */
  public TableJoinColumn(DeployTableJoinColumn deploy) {
    this.localDbColumn = InternString.intern(deploy.getLocalDbColumn());
    this.foreignDbColumn = InternString.intern(deploy.getForeignDbColumn());
    this.localSqlFormula = InternString.intern(deploy.getLocalSqlFormula());
    this.foreignSqlFormula = InternString.intern(deploy.getForeignSqlFormula());
    this.insertable = deploy.isInsertable();
    this.updateable = deploy.isUpdatable();
    this.nullable = deploy.isNullable();
    this.queryHash = hash();
  }

  private TableJoinColumn(TableJoinColumn source, String overrideColumn) {
    this.localDbColumn = InternString.intern(overrideColumn);
    this.foreignDbColumn = source.foreignDbColumn;
    this.localSqlFormula = null;
    this.foreignSqlFormula = null;
    this.insertable = source.isInsertable();
    this.updateable = source.isUpdateable();
    this.nullable = source.isNullable();
    this.queryHash = hash();
  }

  private int hash() {
    int result = localDbColumn != null ? localDbColumn.hashCode() : 0;
    result = 92821 * result + (foreignDbColumn != null ? foreignDbColumn.hashCode() : 0);
    result = 92821 * result + (localSqlFormula != null ? localSqlFormula.hashCode() : 0);
    result = 92821 * result + (foreignSqlFormula != null ? foreignSqlFormula.hashCode() : 0);
    result = 92821 * result + (insertable ? 1 : 0);
    result = 92821 * result + (updateable ? 1 : 0);
    return result;
  }

  @Override
  public int hashCode() {
    return queryHash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TableJoinColumn that = (TableJoinColumn) o;
    if (insertable != that.insertable) return false;
    if (updateable != that.updateable) return false;
    if (!localDbColumn.equals(that.localDbColumn)) return false;
    return foreignDbColumn.equals(that.foreignDbColumn);
  }

  @Override
  public String toString() {
    return (localSqlFormula == null ? localDbColumn : localSqlFormula) + " = "
      + (foreignSqlFormula == null ? foreignDbColumn : foreignSqlFormula);
  }

  /**
   * Return a hash for including in a query plan.
   */
  int queryHash() {
    return queryHash;
  }

  /**
   * Return the foreign database column name.
   */
  public String getForeignDbColumn() {
    return foreignDbColumn;
  }

  /**
   * Return the local database column name.
   */
  public String getLocalDbColumn() {
    return localDbColumn;
  }

  /**
   * Return true if this column should be insertable.
   */
  public boolean isInsertable() {
    return insertable;
  }

  /**
   * Return true if this column should be updateable.
   */
  boolean isUpdateable() {
    return updateable;
  }

  /**
   * Return if this column is nullable.
   */
  public boolean isNullable() {
    return nullable;
  }

  public String getLocalSqlFormula() {
    return localSqlFormula;
  }

  public String getForeignSqlFormula() {
    return foreignSqlFormula;
  }

  TableJoinColumn withOverrideColumn(String overrideColumn) {
    return new TableJoinColumn(this, overrideColumn);
  }
}
