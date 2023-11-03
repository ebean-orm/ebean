package io.ebean.cache;

import io.ebean.BackgroundExecutor;
import io.ebean.DatabaseBuilder;

/**
 * The plugin interface that creates a ServerCacheFactory.
 */
public interface ServerCachePlugin {

  /**
   * Create the ServerCacheFactory given the server config and background executor service.
   */
  ServerCacheFactory create(DatabaseBuilder config, BackgroundExecutor executor);
}
