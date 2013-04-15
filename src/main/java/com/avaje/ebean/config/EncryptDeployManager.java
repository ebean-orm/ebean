package com.avaje.ebean.config;

/**
 * Programmatically define which database columns are encrypted.
 * 
 * @author rbygrave
 * 
 */
public interface EncryptDeployManager {

  /**
   * Return true if the table column is encrypted.
   */
  public EncryptDeploy getEncryptDeploy(TableName table, String column);
}
