package io.ebeaninternal.server.deploy.meta;

import io.ebeaninternal.server.query.SqlJoinType;

import javax.persistence.CascadeType;

/**
 * Property mapped to a joined bean.
 */
public class DeployBeanPropertyAssocOne<T> extends DeployBeanPropertyAssoc<T> {

  private boolean oneToOne;

  private boolean oneToOneExported;

  private boolean primaryKeyJoin;

  private boolean primaryKeyExport;

  private DeployBeanEmbedded deployEmbedded;

  private String columnPrefix;

  private boolean orphanRemoval;

  /**
   * Create the property.
   */
  public DeployBeanPropertyAssocOne(DeployBeanDescriptor<?> desc, Class<T> targetType) {
    super(desc, targetType);
  }

  /**
   * Return the deploy information specifically for the deployment
   * of Embedded beans.
   */
  public DeployBeanEmbedded getDeployEmbedded() {
    // deployment should be single threaded
    if (deployEmbedded == null) {
      deployEmbedded = new DeployBeanEmbedded();
    }
    return deployEmbedded;
  }

  /**
   * Return true if this has multiple properties (expected for embedded id).
   */
  public boolean isCompound() {
    // just checking for compound and not doing numeric check at this stage
    return getDeployEmbedded().getPropertyColumnMap().size() > 1;
  }

  @Override
  public String getDbColumn() {
    DeployTableJoinColumn[] columns = tableJoin.columns();
    if (columns.length == 1) {
      return columns[0].getLocalDbColumn();
    }
    return super.getDbColumn();
  }

  /**
   * Return true if this a OneToOne property. Otherwise assumed ManyToOne.
   */
  public boolean isOneToOne() {
    return oneToOne;
  }

  /**
   * Set to true if this is a OneToOne.
   */
  public void setOneToOne() {
    this.oneToOne = true;
  }

  /**
   * Return true if this is the exported side of a OneToOne.
   */
  public boolean isOneToOneExported() {
    return oneToOneExported;
  }

  /**
   * Set to true if this is the exported side of a OneToOne. This means
   * it doesn't 'own' the foreign key column. A OneToMany without the many.
   */
  public void setOneToOneExported() {
    this.oneToOneExported = true;
  }

  /**
   * Set to true if this is part of the primary key.
   */
  @Override
  public void setImportedPrimaryKeyColumn(DeployBeanProperty primaryKey) {
    this.importedPrimaryKey = true;
    String dbColumn = primaryKey.getDbColumn();
    if (dbColumn != null) {
      // change join db column if matched by property name
      tableJoin.setLocalColumn(dbColumn);
    }
  }

  @Override
  public void setSqlFormula(String formulaSelect, String formulaJoin) {
    super.setSqlFormula(formulaSelect, formulaJoin);
    DeployTableJoinColumn[] columns = tableJoin.columns();
    if (columns.length == 1) {
      columns[0].setLocalSqlFormula(formulaSelect);
    }
  }

  public void setColumnPrefix(String columnPrefix) {
    this.columnPrefix = columnPrefix;
  }

  public String getColumnPrefix() {
    return columnPrefix;
  }

  /**
   * Mark as PrimaryKeyJoin (we don't know which side is the export side initially).
   */
  public void setPrimaryKeyJoin(boolean primaryKeyJoin) {
    this.primaryKeyJoin = primaryKeyJoin;
  }

  public boolean isPrimaryKeyJoin() {
    return primaryKeyJoin;
  }

  public boolean isPrimaryKeyExport() {
    return primaryKeyExport;
  }

  /**
   * Set as export side of OneToOne with PrimaryKeyJoin.
   */
  public void setPrimaryKeyExport() {
    this.primaryKeyExport = true;
    this.oneToOneExported = true;
    if (!cascadeInfo.isSave()) {
      // we pretty much need to cascade save so turning that on automatically ...
      cascadeInfo.setType(CascadeType.ALL);
    }
  }

  public void setOrphanRemoval(boolean orphanRemoval) {
    this.orphanRemoval = orphanRemoval;
  }

  public boolean isOrphanRemoval() {
    return orphanRemoval;
  }

  public void setJoinType(boolean outerJoin) {
    tableJoin.setType(outerJoin ? SqlJoinType.OUTER : SqlJoinType.INNER);
  }

  public void setJoinColumns(DeployTableJoinColumn[] columns, boolean reverse) {
    tableJoin.setColumns(columns, reverse);
  }
}
