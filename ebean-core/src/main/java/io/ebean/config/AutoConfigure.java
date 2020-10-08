package io.ebean.config;

/**
 * Used to provide some automatic configuration early in the creation of a Database.
 */
public interface AutoConfigure {

  /**
   * Perform configuration for the DatabaseConfig prior to properties load.
   */
  void preConfigure(DatabaseConfig config);

  /**
   * Provide some configuration the DatabaseConfig prior to server creation but after properties have been applied.
   */
  void postConfigure(DatabaseConfig config);

}
