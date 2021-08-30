package io.ebeaninternal.server.cache;

import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCachePlugin;
import io.ebean.config.DatabaseConfig;

/**
 * Default implementation of ServerCachePlugin.
 */
public final class DefaultServerCachePlugin implements ServerCachePlugin {

  /**
   * Creates the default ServerCacheFactory.
   */
  @Override
  public ServerCacheFactory create(DatabaseConfig config, BackgroundExecutor executor) {
    return new DefaultServerCacheFactory(executor);
  }
}
