package com.avaje.ebean.event;

import com.avaje.ebean.config.ServerConfig;

/**
 * Used to configure the server on startup.
 * <p>
 * Provides a simple way to construct and register multiple listeners and
 * adapters that need shared services without using DI.
 * </p>
 * 
 * @author Robin Bygrave
 */
public interface ServerConfigStartup {

  /**
   * On starting configure the ServerConfig.
   */
  void onStart(ServerConfig serverConfig);

}
