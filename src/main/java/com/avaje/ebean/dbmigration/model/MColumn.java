package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.Column;

/**
 * A column in the logical model.
 */
public class MColumn {

  private final String name;
  private String type;
  private String checkConstraint;
  private String checkConstraintName;
  private String defaultValue;
  private String references;
  private String foreignKeyName;
  private String foreignKeyIndex;

  private boolean historyExclude;
  private boolean notnull;
  private boolean primaryKey;
  private boolean identity;

  private String unique;

  /**
   * Special unique for OneToOne as we need to handle that different
   * specifically for MsSqlServer.
   */
  private String uniqueOneToOne;

  /**
   * Temporary variable used when building the alter column changes.
   */
  private AlterColumn alterColumn;

  public MColumn(Column column) {
    this.name = column.getName();
    this.type = column.getType();
    this.checkConstraint = column.getCheckConstraint();
    this.checkConstraintName = column.getCheckConstraintName();
    this.defaultValue = column.getDefaultValue();
    this.references = column.getReferences();
    this.foreignKeyName = column.getForeignKeyName();
    this.foreignKeyIndex = column.getForeignKeyIndex();
    this.notnull = Boolean.TRUE.equals(column.isNotnull());
    this.primaryKey = Boolean.TRUE.equals(column.isPrimaryKey());
    this.identity = Boolean.TRUE.equals(column.isIdentity());
    this.unique = column.getUnique();
    this.uniqueOneToOne = column.getUniqueOneToOne();
    this.historyExclude = Boolean.TRUE.equals(column.isHistoryExclude());
  }

  public MColumn(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public MColumn(String name, String type, boolean notnull) {
    this.name = name;
    this.type = type;
    this.notnull = notnull;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public boolean isPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(boolean primaryKey) {
    this.primaryKey = primaryKey;
  }

  public boolean isIdentity() {
    return identity;
  }

  public void setIdentity(boolean identity) {
    this.identity = identity;
  }

  public String getCheckConstraint() {
    return checkConstraint;
  }

  public void setCheckConstraint(String checkConstraint) {
    this.checkConstraint = checkConstraint;
  }

  public String getCheckConstraintName() {
    return checkConstraintName;
  }

  public void setCheckConstraintName(String checkConstraintName) {
    this.checkConstraintName = checkConstraintName;
  }

  public String getForeignKeyName() {
    return foreignKeyName;
  }

  public void setForeignKeyName(String foreignKeyName) {
    this.foreignKeyName = foreignKeyName;
  }

  public String getForeignKeyIndex() {
    return foreignKeyIndex;
  }

  public void setForeignKeyIndex(String foreignKeyIndex) {
    this.foreignKeyIndex = foreignKeyIndex;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public String getReferences() {
    return references;
  }

  public void setReferences(String references) {
    this.references = references;
  }

  public boolean isNotnull() {
    return notnull;
  }

  public void setNotnull(boolean notnull) {
    this.notnull = notnull;
  }

  public boolean isHistoryExclude() {
    return historyExclude;
  }

  public void setHistoryExclude(boolean historyExclude) {
    this.historyExclude = historyExclude;
  }

  public void setUnique(String unique) {
    this.unique = unique;
  }

  public String getUnique() {
    return unique;
  }

  /**
   * Set unique specifically for OneToOne mapping.
   * We need special DDL for this case for MsSqlServer.
   */
  public void setUniqueOneToOne(String uniqueOneToOne) {
    this.uniqueOneToOne = uniqueOneToOne;
  }

  /**
   * Return true if this is unique for a OneToOne.
   */
  public String getUniqueOneToOne() {
    return uniqueOneToOne;
  }

  public Column createColumn() {

    Column c = new Column();
    c.setName(name);
    c.setType(type);

    if (notnull) c.setNotnull(true);
    if (primaryKey) c.setPrimaryKey(true);
    if (identity) c.setIdentity(true);
    if (historyExclude) c.setHistoryExclude(true);

    c.setCheckConstraint(checkConstraint);
    c.setCheckConstraintName(checkConstraintName);
    c.setReferences(references);
    c.setForeignKeyName(foreignKeyName);
    c.setForeignKeyIndex(foreignKeyIndex);
    c.setDefaultValue(defaultValue);
    c.setUnique(unique);
    c.setUniqueOneToOne(uniqueOneToOne);

    return c;
  }

  private boolean different(String val1, String val2) {
    return (val1 == null) ? val2 != null : !val1.equals(val2);
  }

  private boolean hasValue(String val) {
    return val != null && !val.isEmpty();
  }

  private AlterColumn getAlterColumn(String tableName, boolean tableWithHistory) {
    if (alterColumn == null) {
      alterColumn = new AlterColumn();
      alterColumn.setColumnName(name);
      alterColumn.setTableName(tableName);
      if (tableWithHistory) {
        alterColumn.setWithHistory(Boolean.TRUE);
      }
    }
    return alterColumn;
  }

  /**
   * Compare the column meta data and return true if there is a change that means
   * the history table column needs

   */
  public void compare(ModelDiff modelDiff, MTable table, MColumn newColumn) {

    boolean tableWithHistory = table.isWithHistory();
    String tableName = table.getName();

    // set to null and check at the end
    this.alterColumn = null;

    boolean changeBaseAttribute = false;

    if (historyExclude != newColumn.historyExclude) {
      getAlterColumn(tableName, tableWithHistory).setHistoryExclude(newColumn.historyExclude);
    }

    if (different(type, newColumn.type)) {
      changeBaseAttribute = true;
      getAlterColumn(tableName, tableWithHistory).setType(newColumn.type);
    }
    if (notnull != newColumn.notnull) {
      changeBaseAttribute = true;
      getAlterColumn(tableName, tableWithHistory).setNotnull(newColumn.notnull);
    }
    if (different(defaultValue, newColumn.defaultValue)) {
      AlterColumn alter = getAlterColumn(tableName, tableWithHistory);
      if (newColumn.defaultValue == null) {
        alter.setDefaultValue("DROP DEFAULT");
      } else {
        alter.setDefaultValue(newColumn.defaultValue);
      }
    }

    if (different(checkConstraint, newColumn.checkConstraint)) {
      AlterColumn alter = getAlterColumn(tableName, tableWithHistory);
      if (hasValue(checkConstraint)) {
        alter.setDropCheckConstraint(checkConstraintName);
      }
      if (hasValue(newColumn.checkConstraint)) {
        alter.setCheckConstraintName(newColumn.checkConstraintName);
        alter.setCheckConstraint(newColumn.checkConstraint);
      }
    }
    if (different(references, newColumn.references)) {
      // foreign key change
      AlterColumn alter = getAlterColumn(tableName, tableWithHistory);
      if (hasValue(foreignKeyName)) {
        alter.setDropForeignKey(foreignKeyName);
      }
      if (hasValue(foreignKeyIndex)) {
        alter.setDropForeignKeyIndex(foreignKeyIndex);
      }
      if (hasValue(newColumn.references)) {
        // add new foreign key constraint
        alter.setReferences(newColumn.references);
        alter.setForeignKeyName(newColumn.foreignKeyName);
        alter.setForeignKeyIndex(newColumn.foreignKeyIndex);
      }
    }

    if (different(unique, newColumn.unique)) {
      AlterColumn alter = getAlterColumn(tableName, tableWithHistory);
      if (hasValue(unique)) {
        alter.setDropUnique(unique);
      }
      if (hasValue(newColumn.unique)) {
        alter.setUnique(newColumn.unique);
      }
    }
    if (different(uniqueOneToOne, newColumn.uniqueOneToOne)) {
      AlterColumn alter = getAlterColumn(tableName, tableWithHistory);
      if (hasValue(uniqueOneToOne)) {
        alter.setDropUnique(uniqueOneToOne);
      }
      if (hasValue(newColumn.uniqueOneToOne)) {
        alter.setUniqueOneToOne(newColumn.uniqueOneToOne);
      }
    }

    if (alterColumn != null) {
      modelDiff.addAlterColumn(alterColumn);
      if (changeBaseAttribute) {
        // support reverting these changes
        alterColumn.setCurrentType(type);
        alterColumn.setCurrentNotnull(notnull);
      }
    }
  }

  /**
   * Apply changes based on the AlterColumn request.
   */
  public void apply(AlterColumn alterColumn) {

    if (hasValue(alterColumn.getDropCheckConstraint())) {
      checkConstraint = null;
    }
    if (hasValue(alterColumn.getDropForeignKey())) {
      foreignKeyName = null;
    }
    if (hasValue(alterColumn.getDropForeignKeyIndex())) {
      foreignKeyIndex = null;
    }
    if (hasValue(alterColumn.getDropUnique())) {
      unique = null;
      uniqueOneToOne = null;
    }

    if (hasValue(alterColumn.getType())) {
      type = alterColumn.getType();
    }
    if (hasValue(alterColumn.getDefaultValue())) {
      defaultValue = alterColumn.getDefaultValue();
    }
    if (hasValue(alterColumn.getCheckConstraint())) {
      checkConstraint = alterColumn.getCheckConstraint();
    }
    if (hasValue(alterColumn.getCheckConstraintName())) {
      checkConstraintName = alterColumn.getCheckConstraintName();
    }
    if (hasValue(alterColumn.getUnique())) {
      unique = alterColumn.getUnique();
    }
    if (hasValue(alterColumn.getUniqueOneToOne())) {
      uniqueOneToOne = alterColumn.getUniqueOneToOne();
    }
    if (hasValue(alterColumn.getReferences())) {
      references = alterColumn.getReferences();
    }
    if (hasValue(alterColumn.getForeignKeyName())) {
      foreignKeyName = alterColumn.getForeignKeyName();
    }
    if (hasValue(alterColumn.getForeignKeyIndex())) {
      foreignKeyIndex = alterColumn.getForeignKeyIndex();
    }

  }
}
