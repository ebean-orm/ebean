package com.avaje.ebean.cache;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.config.ServerConfig;

/**
 * The plugin interface that creates a ServerCacheFactory.
 */
public interface ServerCachePlugin {

  /**
   * Create the ServerCacheFactory given the server config and background executor service.
   */
  ServerCacheFactory create(ServerConfig config, BackgroundExecutor executor);
}
