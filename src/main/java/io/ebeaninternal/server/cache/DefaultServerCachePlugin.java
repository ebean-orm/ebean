package io.ebeaninternal.server.cache;

import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCachePlugin;
import io.ebean.config.ServerConfig;

/**
 * Default implementation of ServerCachePlugin.
 */
public class DefaultServerCachePlugin implements ServerCachePlugin {

  /**
   * Creates the default ServerCacheFactory.
   */
  @Override
  public ServerCacheFactory create(ServerConfig config, BackgroundExecutor executor) {
    return new DefaultServerCacheFactory(executor);
  }
}
