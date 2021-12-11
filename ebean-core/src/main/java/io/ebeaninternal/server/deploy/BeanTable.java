package io.ebeaninternal.server.deploy;

import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.server.core.InternString;
import io.ebeaninternal.server.deploy.meta.DeployBeanTable;
import io.ebeaninternal.server.deploy.meta.DeployTableJoin;
import io.ebeaninternal.server.deploy.meta.DeployTableJoinColumn;


/**
 * Used for associated beans in place of a BeanDescriptor. This is done to avoid
 * recursion issues due to the potentially bi-directional and circular
 * relationships between beans.
 * <p>
 * It holds the main deployment information and not all the detail that is held
 * in a BeanDescriptor.
 * </p>
 */
public final class BeanTable {

  private final BeanDescriptorMap owner;
  private final Class<?> beanType;
  private final String baseTable;
  private final BeanProperty idProperty;

  /**
   * Create the BeanTable.
   */
  public BeanTable(DeployBeanTable mutable, BeanDescriptorMap owner) {
    this.owner = owner;
    this.beanType = mutable.getBeanType();
    this.baseTable = InternString.intern(mutable.getBaseTable());
    this.idProperty = mutable.createIdProperty(owner);
  }

  /**
   * Construct for element collection.
   */
  public BeanTable(BeanDescriptorMap owner, String tableName, Class<?> beanType) {
    this.owner = owner;
    this.beanType = beanType;
    this.baseTable = tableName;
    this.idProperty = null;
  }

  @Override
  public String toString() {
    return baseTable;
  }

  /**
   * Return the base table for this BeanTable.
   * This is used to determine the join information
   * for associations.
   */
  public String getBaseTable() {
    return baseTable;
  }

  /**
   * Gets the unqualified base table.
   *
   * @return the unqualified base table
   */
  public String getUnqualifiedBaseTable() {
    final String[] chunks = baseTable.split("\\.");
    return chunks.length == 2 ? chunks[1] : chunks[0];
  }

  /**
   * Return the Id properties.
   */
  public BeanProperty getIdProperty() {
    return idProperty;
  }

  /**
   * Return the class for this beanTable.
   */
  public Class<?> getBeanType() {
    return beanType;
  }

  public void createJoinColumn(String foreignKeyPrefix, DeployTableJoin join, boolean reverse, String sqlFormulaSelect) {
    if (idProperty == null) {
      return;
    }
    if (idProperty instanceof BeanPropertyAssocOne<?>) {
      BeanPropertyAssocOne<?> assocOne = (BeanPropertyAssocOne<?>) idProperty;
      BeanProperty[] props = assocOne.properties();
      for (BeanProperty prop : props) {
        addToJoin(foreignKeyPrefix, join, reverse, sqlFormulaSelect, true, prop);
      }
    } else {
      addToJoin(foreignKeyPrefix, join, reverse, sqlFormulaSelect, false, idProperty);
    }
  }

  private void addToJoin(String foreignKeyPrefix, DeployTableJoin join, boolean reverse, String sqlFormulaSelect, boolean complexKey, BeanProperty prop) {
    String lc = prop.dbColumn();
    String fk = lc;
    if (foreignKeyPrefix != null) {
      fk = owner.namingConvention().getForeignKey(foreignKeyPrefix, fk);
    }
    if (complexKey) {
      // just to copy the column name rather than prefix with the foreignKeyPrefix.
      // I think that with complex keys this is the more common approach.
      CoreLog.internal.debug("On table[{}] foreign key column [{}]", baseTable, lc);
      fk = lc;
    }
    if (sqlFormulaSelect != null) {
      fk = sqlFormulaSelect;
    }
    DeployTableJoinColumn joinCol = new DeployTableJoinColumn(lc, fk);
    joinCol.setForeignSqlFormula(sqlFormulaSelect);
    if (reverse) {
      joinCol = joinCol.reverse();
    }
    join.addJoinColumn(joinCol);
  }

  /**
   * Return the primary key DB column.
   */
  public String getIdColumn() {
    return idProperty.dbColumn;
  }
}
