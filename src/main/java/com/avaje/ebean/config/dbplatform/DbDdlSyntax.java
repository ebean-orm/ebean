package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.config.TableName;

/**
 * Used to support DB specific syntax for DDL generation.
 */
public class DbDdlSyntax {

  protected boolean renderIndexForFkey = true;

  protected boolean inlinePrimaryKeyConstraint = false;

  protected boolean addOneToOneUniqueContraint = true;

  protected int maxConstraintNameLength = 32;

  protected int columnNameWidth = 25;

  /**
   * Flag set when we want to disable constraints on each table (rather than globally).
   */
  protected boolean dropKeyConstraints;

  protected String dropTableCascade;
  protected String dropIfExists;

  protected String newLine = "\n";

  protected String identity = "auto_increment";
  protected String identitySuffix = "";

  protected String pkPrefix = "pk_";

  protected String disableReferentialIntegrity;
  protected String enableReferentialIntegrity;

  protected String foreignKeySuffix;

  /**
   * Return the primary key name for a given bean descriptor.
   */
  public String getPrimaryKeyName(String tableName) {

    String pk = pkPrefix + tableName;
    if (pk.length() > maxConstraintNameLength) {
      // need to trim the primary key name
      pk = pk.substring(0, maxConstraintNameLength);
    }
    return pk;
  }

  /**
   * Return the column definition for an identity column.
   */
  public String getIdentityColumnDefn(String columnDefn) {
    String identity = getIdentity();
    if (identity != null && identity.length() > 0) {
      return columnDefn+" "+identity;
    }
    return columnDefn;
  }
  
  /**
   * Return the identity clause for DB's that have identities.
   */
  public String getIdentity() {
    return identity;
  }

  /**
   * Set the identity clause.
   */
  public void setIdentity(String identity) {
    this.identity = identity;
  }

  /**
   * Typically returns empty string but for SQLite and perhaps others
   * the Identity/AutoIncrement clause comes after the primary key clause.
   */
  public String getIdentitySuffix() {
    return identitySuffix;
  }

  /**
   * Set the identity clause that would appear after the primary key clause.
   * <p>
   * Only used for SQLite at this stage.
   * </p>
   */
  public void setIdentitySuffix(String identitySuffix) {
    this.identitySuffix = identitySuffix;
  }

  /**
   * Return the width for padding whitespace after column names.
   */
  public int getColumnNameWidth() {
    return columnNameWidth;
  }

  /**
   * Set the amount of padding to write after the column name.
   */
  public void setColumnNameWidth(int columnNameWidth) {
    this.columnNameWidth = columnNameWidth;
  }

  /**
   * Return the new line character.
   */
  public String getNewLine() {
    return newLine;
  }

  /**
   * Set the new line character.
   */
  public void setNewLine(String newLine) {
    this.newLine = newLine;
  }

  /**
   * Return the prefix used in naming primary keys.
   */
  public String getPkPrefix() {
    return pkPrefix;
  }

  /**
   * Set the prefix used in naming primary keys.
   */
  public void setPkPrefix(String pkPrefix) {
    this.pkPrefix = pkPrefix;
  }

  /**
   * Return the DB specific command to disable referential integrity
   */
  public String getDisableReferentialIntegrity() {
    return disableReferentialIntegrity;
  }

  /**
   * Set the DB specific command to disable referential integrity
   */
  public void setDisableReferentialIntegrity(String disableReferentialIntegrity) {
    this.disableReferentialIntegrity = disableReferentialIntegrity;
  }

  /**
   * Return the DB specific command to enable referential integrity
   */
  public String getEnableReferentialIntegrity() {
    return enableReferentialIntegrity;
  }

  /**
   * Set the DB specific command to enable referential integrity
   */
  public void setEnableReferentialIntegrity(String enableReferentialIntegrity) {
    this.enableReferentialIntegrity = enableReferentialIntegrity;
  }

  /**
   * Return true to is constraints are disabled on each table.
   */
  public boolean isDropKeyConstraints() {
    return dropKeyConstraints;
  }

  /**
   * Return some DDL to disable constraints on the given table.
   */
  public String dropKeyConstraintPrefix(String tableName, String fkName) {
    return null;
  }

  /**
   * Return true if indexes should be created for the foreign keys.
   */
  public boolean isRenderIndexForFkey() {
    return renderIndexForFkey;
  }

  /**
   * Set whether indexes should be created for the foreign keys.
   */
  public void setRenderIndexForFkey(boolean renderIndexForFkey) {
    this.renderIndexForFkey = renderIndexForFkey;
  }

  /**
   * Typically returns IF EXISTS (if that is supported by the database platform)
   * or null.
   */
  public String getDropIfExists() {
    return dropIfExists;
  }

  /**
   * Set the IF EXISTS clause for dropping tables if that is supported by the
   * database platform.
   */
  public void setDropIfExists(String dropIfExists) {
    this.dropIfExists = dropIfExists;
  }

  /**
   * Return prefix text that goes before drop table.
   */
  public String dropTablePrefix(String tableName) {
    return "";
  }

  /**
   * Return the cascade option for the drop table command.
   */
  public String getDropTableCascade() {
    return dropTableCascade;
  }

  /**
   * Set the cascade option for the drop table command.
   */
  public void setDropTableCascade(String dropTableCascade) {
    this.dropTableCascade = dropTableCascade;
  }

  /**
   * Return the foreign key suffix.
   */
  public String getForeignKeySuffix() {
    return foreignKeySuffix;
  }

  /**
   * Set the foreign key suffix.
   */
  public void setForeignKeySuffix(String foreignKeySuffix) {
    this.foreignKeySuffix = foreignKeySuffix;
  }

  /**
   * Return the maximum length that constraint names can be for this database.
   */
  public int getMaxConstraintNameLength() {
    return maxConstraintNameLength;
  }

  /**
   * Set the maximum length that constraint names can be for this database.
   */
  public void setMaxConstraintNameLength(int maxFkeyLength) {
    this.maxConstraintNameLength = maxFkeyLength;
  }

  /**
   * Return true if imported side of OneToOne's should have unique constraints
   * generated.
   */
  public boolean isAddOneToOneUniqueContraint() {
    return addOneToOneUniqueContraint;
  }

  /**
   * Set to false for DB's that don't want both a unique and index on the
   * imported OneToOne.
   */
  public void setAddOneToOneUniqueContraint(boolean addOneToOneUniqueContraint) {
    this.addOneToOneUniqueContraint = addOneToOneUniqueContraint;
  }

  /**
   * Return true if primary key constraints should be inlined when they are a
   * single column.
   */
  public boolean isInlinePrimaryKeyConstraint() {
    return inlinePrimaryKeyConstraint;
  }

  /**
   * Set whether to inline primary key constraints.
   */
  public void setInlinePrimaryKeyConstraint(boolean inlinePrimaryKeyConstraint) {
    this.inlinePrimaryKeyConstraint = inlinePrimaryKeyConstraint;
  }

  /**
   * Builds and returns a fully index name.
   */
  public String getIndexName(String table, String propName, int ixCount) {


    StringBuilder buffer = new StringBuilder(30);
    buffer.append("ix_").append(TableName.parse(table));
    buffer.append("_").append(propName);

    addSuffix(buffer, ixCount);

    return buffer.toString();
  }

  /**
   * Builds and returns a fully qualified foreign key constraint name.
   */
  public String getForeignKeyName(String table, String propName, int fkCount) {

    StringBuilder buffer = new StringBuilder(30);
    buffer.append("fk_").append(TableName.parse(table));
    buffer.append("_").append(propName);

    addSuffix(buffer, fkCount);

    return buffer.toString();
  }

  /**
   * Adds the suffix.
   */
  protected void addSuffix(StringBuilder buffer, int count) {
    String suffixNr = Integer.toString(count);
    int suffixLen = suffixNr.length() + 1;

    if (buffer.length() + suffixLen > maxConstraintNameLength) {
      buffer.setLength(maxConstraintNameLength - suffixLen);
    }
    buffer.append("_");
    buffer.append(suffixNr);
  }

}
