package com.avaje.ebean.config.dbplatform;

/**
 * Defines DB encryption support for encrypting and decrypting data using DB
 * encryption features.
 * <p>
 * As an alternative to using DB encryption you can encrypt/decrypt in java via
 * a special ScalarType but this has the limitation that you can't include that
 * property in query where clauses.
 * </p>
 * 
 * @author rbygrave
 */
public interface DbEncrypt {

  // /**
  // * Return the SQL for decrypting a column returning a VARCHAR.
  // */
  // String getDecryptSql(String columnWithTableAlias);
  //
  // /**
  // * Return the DB function with bind variables used to encrypt a VARCHAR
  // * value.
  // */
  // String getEncryptBindSql();

  DbEncryptFunction getDbEncryptFunction(int jdbcType);

  /**
   * Return the DB type that encrypted Strings are stored in.
   * <p>
   * This is VARCHAR for MySql and VARBINARY for most others.
   * </p>
   */
  int getEncryptDbType();

  /**
   * Return true if the DB encrypt function binds the data before the key.
   */
  boolean isBindEncryptDataFirst();
}