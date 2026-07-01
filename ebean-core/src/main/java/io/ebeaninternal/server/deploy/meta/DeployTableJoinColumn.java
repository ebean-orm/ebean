package io.ebeaninternal.server.deploy.meta;

import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanTable;

/**
 * A join pair of local and foreign properties.
 */
public final class DeployTableJoinColumn {

  /**
   * The local database column name.
   */
  private String localDbColumn;
  /**
   * SQL formula used for local column
   */
  private String localSqlFormula;
  /**
   * The foreign database column name.
   */
  private String foreignDbColumn;
  /**
   * SQL formula used for foreign column
   */
  private String foreignSqlFormula;
  private boolean insertable;
  private boolean updatable;
  private boolean nullable;

  /**
   * Construct when automatically determining the join.
   * <p>
   * Assume that we want the foreign key to be insertable and updateable.
   * </p>
   */
  public DeployTableJoinColumn(String localDbColumn, String foreignDbColumn) {
    this(localDbColumn, foreignDbColumn, true, true, true);
  }

  /**
   * Construct with explicit insertable and updateable flags.
   */
  public DeployTableJoinColumn(String localDbColumn, String foreignDbColumn,
                               boolean insertable, boolean updatable, boolean nullable) {
    this.localDbColumn = nullEmptyString(localDbColumn);
    this.foreignDbColumn = nullEmptyString(foreignDbColumn);
    this.insertable = insertable;
    this.updatable = updatable;
    this.nullable = nullable;
  }

  public DeployTableJoinColumn(boolean order, String ref, String name,
                               boolean insertable, boolean updatable, boolean nullable, BeanTable beanTable) {
    this(ref, name, insertable, updatable, nullable);
    setReferencedColumn(beanTable);
    if (!order) {
      reverse();
    }
  }

  void setLocalSqlFormula(String localSqlFormula) {
    if (localSqlFormula != null) {
      this.localSqlFormula = localSqlFormula;
      this.localDbColumn = null;
      this.insertable = false;
      this.updatable = false;
    }
  }

  public String getLocalSqlFormula() {
    return localSqlFormula;
  }

  public void setForeignSqlFormula(String foreignSqlFormula) {
    if (foreignSqlFormula != null) {
      this.foreignSqlFormula = foreignSqlFormula;
      this.foreignDbColumn = null;
      this.insertable = false;
      this.updatable = false;
    }
  }

  public String getForeignSqlFormula() {
    return foreignSqlFormula;
  }

  private void setReferencedColumn(BeanTable beanTable) {
    if (localDbColumn == null) {
      BeanProperty idProperty = beanTable.getIdProperty();
      if (idProperty != null) {
        localDbColumn = idProperty.dbColumn();
      }
    }
  }

  /**
   * Reverse the direction of the join.
   */
  public DeployTableJoinColumn reverse() {
    String temp = localDbColumn;
    localDbColumn = foreignDbColumn;
    foreignDbColumn = temp;

    temp = localSqlFormula;
    localSqlFormula = foreignSqlFormula;
    foreignSqlFormula = temp;
    return this;
  }

  /**
   * Helper method to null out empty strings.
   */
  private String nullEmptyString(String s) {
    if ("".equals(s)) {
      return null;
    }
    return s;
  }


  public DeployTableJoinColumn copy(boolean reverse) {
    // Note that the insertable and updateable are just copied
    // which may not always be the correct thing to do
    // but will leave it like this for now
    DeployTableJoinColumn ret;
    if (reverse) {
      ret = new DeployTableJoinColumn(foreignDbColumn, localDbColumn, insertable, updatable, nullable);
      ret.setLocalSqlFormula(foreignSqlFormula);
      ret.setForeignSqlFormula(localSqlFormula);

    } else {
      ret = new DeployTableJoinColumn(localDbColumn, foreignDbColumn, insertable, updatable, nullable);
      ret.setLocalSqlFormula(localSqlFormula);
      ret.setForeignSqlFormula(foreignSqlFormula);
    }
    return ret;
  }

  @Override
  public String toString() {
    return localDbColumn + " = " + foreignDbColumn;
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
  public boolean isUpdatable() {
    return updatable;
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
   * Set the local database column name.
   */
  void setLocalDbColumn(String localDbColumn) {
    this.localDbColumn = localDbColumn;
  }

  public boolean isNullable() {
    return nullable;
  }
}
