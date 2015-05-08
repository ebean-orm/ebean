package com.avaje.ebean.config.dbplatform;

import java.sql.Types;

/**
 * Base type for DB platform specific Encryption.
 * <p>
 * DB specific classes that extend this need to set their specific encryption
 * functions for varchar, date and timestamp. If they are left null then that is
 * treated as though that data type can not be encrypted in the DB and will
 * instead use java client encryption.
 * </p>
 * 
 * @author rbygrave
 */
public abstract class AbstractDbEncrypt implements DbEncrypt {

  /**
   * The encryption function for all String types (VARCHAR, CLOB, LONGVARCHAR,
   * CHAR).
   */
  protected DbEncryptFunction varcharEncryptFunction;

  /**
   * The encryption function for all Date types (java.sql.Date, Joda Date
   * types).
   */
  protected DbEncryptFunction dateEncryptFunction;

  /**
   * The encryption function for all Timestamp types (java.sql.Timestamp,
   * java.util.Date, java.util.Calendar, Joda DateTime types etc).
   */
  protected DbEncryptFunction timestampEncryptFunction;

  /**
   * Return the DB encryption function for the given JDBC type.
   * <p>
   * Null is returned if DB encryption of the type is not supported.
   * </p>
   */
  public DbEncryptFunction getDbEncryptFunction(int jdbcType) {
    switch (jdbcType) {
    case Types.VARCHAR:
      return varcharEncryptFunction;
    case Types.CLOB:
      return varcharEncryptFunction;
    case Types.CHAR:
      return varcharEncryptFunction;
    case Types.LONGVARCHAR:
      return varcharEncryptFunction;

    case Types.DATE:
      return dateEncryptFunction;

    case Types.TIMESTAMP:
      return timestampEncryptFunction;

    default:
      return null;
    }
  }

  /**
   * Return the DB stored type for encrypted properties.
   */
  public int getEncryptDbType() {
    return Types.VARBINARY;
  }

  /**
   * Generally encrypt function binding the data before the key (except h2).
   */
  public boolean isBindEncryptDataFirst() {
    return true;
  }
}
