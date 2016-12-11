package io.ebean.event;

import io.ebean.config.ServerConfig;

/**
 * Used to configure the server on startup.
 * <p>
 * Provides a simple way to construct and register multiple listeners and
 * adapters that need shared services without using DI.
 */
public interface ServerConfigStartup {

  /**
   * On starting configure the ServerConfig.
   */
  void onStart(ServerConfig serverConfig);

}
