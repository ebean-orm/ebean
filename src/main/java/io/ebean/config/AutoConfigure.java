package io.ebean.config;

/**
 * Used to provide some automatic configuration early in the creation of an EbeanServer.
 */
public interface AutoConfigure {

  /**
   * Perform configuration for the ServerConfig prior to properties load.
   */
  void preConfigure(ServerConfig serverConfig);

  /**
   * Provide some configuration the ServerConfig prior to server creation but after properties have been applied.
   */
  void postConfigure(ServerConfig serverConfig);

}
