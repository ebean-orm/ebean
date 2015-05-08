package com.avaje.ebean.config.dbplatform;

public interface DbEncryptFunction {

  /**
   * Return the SQL for decrypting a column returning a VARCHAR.
   */
  String getDecryptSql(String columnWithTableAlias);

  /**
   * Return the DB function with bind variables used to encrypt a VARCHAR value.
   */
  String getEncryptBindSql();

}
