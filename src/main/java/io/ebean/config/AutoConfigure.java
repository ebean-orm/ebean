package io.ebean.config;

/**
 * Used to provide some automatic configuration early in the creation of an EbeanServer.
 */
public interface AutoConfigure {

  /**
   * Provide some configuration the ServerConfig prior to server creation.
   * <p>
   * Return true if the autoConfiguration applies to this ServerConfig.
   */
  void configure(ServerConfig serverConfig);

}
