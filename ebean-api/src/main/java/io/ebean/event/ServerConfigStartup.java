package io.ebean.event;

import io.ebean.config.DatabaseConfig;

/**
 * Used to configure the server on startup.
 * <p>
 * Provides a simple way to construct and register multiple listeners and
 * adapters that need shared services without using DI.
 */
public interface ServerConfigStartup {

  /**
   * On starting configure the DatabaseConfig.
   */
  void onStart(DatabaseConfig config);

}
