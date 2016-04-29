package com.avaje.ebeaninternal.server.cache;

import com.avaje.ebean.BackgroundExecutor;
import com.avaje.ebean.cache.ServerCacheFactory;
import com.avaje.ebean.cache.ServerCachePlugin;
import com.avaje.ebean.config.ServerConfig;

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
