package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.Column;

/**
 * A column in the logical model.
 */
public class MColumn {

  private final String name;
  private final String type;
  private String checkConstraint;
  private String defaultValue;
  private String references;
  private boolean notnull;
  private boolean primaryKey;
  private boolean identity;
  private boolean unique;

  public MColumn(Column column) {
    this.name = column.getName();
    this.type = column.getType();
    this.checkConstraint = column.getCheckConstraint();
    this.defaultValue = column.getDefaultValue();
    this.references = column.getReferences();
    this.notnull = Boolean.TRUE.equals(column.isNotnull());
    this.primaryKey = Boolean.TRUE.equals(column.isPrimaryKey());
    this.identity = Boolean.TRUE.equals(column.isIdentity());
    this.unique = Boolean.TRUE.equals(column.isUnique());
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

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public boolean isUnique() {
    return unique;
  }

  public Column createColumn() {

    Column c = new Column();
    c.setName(name);
    c.setType(type);
    c.setNotnull(notnull);
    c.setCheckConstraint(checkConstraint);
    c.setUnique(unique);
    c.setPrimaryKey(primaryKey);
    c.setIdentity(identity);
    c.setReferences(references);
    c.setDefaultValue(defaultValue);

    //c.setDeleteCascade();
    //c.setDeferrable(deferrable);

    return c;
  }
}
