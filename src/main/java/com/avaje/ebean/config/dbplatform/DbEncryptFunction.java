package com.avaje.ebean.config.dbplatform;

public interface DbEncryptFunction {

  /**
   * Return the SQL for decrypting a column returning a VARCHAR.
   */
  public String getDecryptSql(String columnWithTableAlias);

  /**
   * Return the DB function with bind variables used to encrypt a VARCHAR value.
   */
  public String getEncryptBindSql();

}
