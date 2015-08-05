package com.avaje.ebean.config.dbplatform;

import com.avaje.ebean.dbmigration.migration.IdentityType;

/**
 * Defines the identity/sequence behaviour for the database.
 */
public class DbIdentity {

  /**
   * Set if this DB supports sequences. Note some DB's support both Sequences
   * and Identity.
   */
  private boolean supportsSequence;
  private boolean supportsIdentity;

  private boolean supportsGetGeneratedKeys;

  private String selectLastInsertedIdTemplate;

  private IdType idType = IdType.IDENTITY;

  public DbIdentity() {
  }

  /**
   * Return true if GetGeneratedKeys is supported.
   * <p>
   * GetGeneratedKeys required to support JDBC batching transparently.
   * </p>
   */
  public boolean isSupportsGetGeneratedKeys() {
    return supportsGetGeneratedKeys;
  }

  /**
   * Set if GetGeneratedKeys is supported.
   */
  public void setSupportsGetGeneratedKeys(boolean supportsGetGeneratedKeys) {
    this.supportsGetGeneratedKeys = supportsGetGeneratedKeys;
  }

  /**
   * Return the SQL query to find the SelectLastInsertedId.
   * <p>
   * This should only be set on databases that don't support GetGeneratedKeys.
   * </p>
   */
  public String getSelectLastInsertedId(String table) {
    if (selectLastInsertedIdTemplate == null) {
      return null;
    }
    return selectLastInsertedIdTemplate.replace("{table}", table);
  }

  /**
   * Set the template used to build the SQL query to return the LastInsertedId.
   * <p>
   * The template can contain "{table}" where the table name should be include
   * in the sql query.
   * </p>
   * <p>
   * This should only be set on databases that don't support GetGeneratedKeys.
   * </p>
   */
  public void setSelectLastInsertedIdTemplate(String selectLastInsertedIdTemplate) {
    this.selectLastInsertedIdTemplate = selectLastInsertedIdTemplate;
  }

  /**
   * Return true if the database supports sequences.
   */
  public boolean isSupportsSequence() {
    return supportsSequence;
  }

  /**
   * Set to true if the database supports sequences. Generally this also means
   * you want to set the default IdType to sequence (some DB's support both
   * sequences and identity).
   */
  public void setSupportsSequence(boolean supportsSequence) {
    this.supportsSequence = supportsSequence;
  }

  /**
   * Return true if this DB platform supports identity (autoincrement).
   */
  public boolean isSupportsIdentity() {
    return supportsIdentity;
  }

  /**
   * Set to true if this DB platform supports identity (autoincrement).
   */
  public void setSupportsIdentity(boolean supportsIdentity) {
    this.supportsIdentity = supportsIdentity;
  }

  /**
   * Return the default ID generation type that should be used. This should be
   * either SEQUENCE or IDENTITY (aka Autoincrement).
   * <p>
   * Note: Id properties of type UUID automatically get a UUID generator
   * assigned to them.
   * </p>
   */
  public IdType getIdType() {
    return idType;
  }

  /**
   * Set the default ID generation type that should be used.
   */
  public void setIdType(IdType idType) {
    this.idType = idType;
  }

  /**
   * Determine the id type to use based on requested identityType and
   * the support for that in the database platform.
   */
  public IdType useIdentityType(IdentityType identityType) {

    if (identityType == null) {
      // use the default
      return idType;
    }
    switch (identityType) {
      case GENERATOR:
        return IdType.GENERATOR;
      case EXTERNAL:
        return IdType.EXTERNAL;
      case SEQUENCE:
        return supportsSequence ? IdType.SEQUENCE : idType;
      case IDENTITY:
        return supportsIdentity ? IdType.IDENTITY : idType;
    }

    // use the default
    return idType;
  }
}
