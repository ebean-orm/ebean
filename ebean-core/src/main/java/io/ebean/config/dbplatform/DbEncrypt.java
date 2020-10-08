package io.ebean.config.dbplatform;

/**
 * Defines DB encryption support for encrypting and decrypting data using DB
 * encryption features.
 * <p>
 * As an alternative to using DB encryption you can encrypt/decrypt in java via
 * a special ScalarType but this has the limitation that you can't include that
 * property in query where clauses.
 */
public interface DbEncrypt {

  /**
   * Return the DB encrypt function for the given JDBC type.
   */
  DbEncryptFunction getDbEncryptFunction(int jdbcType);

  /**
   * Return the DB type that encrypted Strings are stored in.
   * <p>
   * This is VARCHAR for MySql and VARBINARY for most others.
   */
  int getEncryptDbType();

  /**
   * Return true if the DB encrypt function binds the data before the key.
   */
  boolean isBindEncryptDataFirst();
}
