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
  EncryptDeploy getEncryptDeploy(TableName table, String column);
}
