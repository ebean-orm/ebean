package io.ebean.config;

/**
 * Programmatically define which database columns are encrypted.
 */
@FunctionalInterface
public interface EncryptDeployManager {

  /**
   * Return true if the table column is encrypted.
   */
  EncryptDeploy getEncryptDeploy(TableName table, String column);
}
