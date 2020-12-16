package io.ebean.redis;

import io.ebean.BackgroundExecutor;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCachePlugin;
import io.ebean.config.DatabaseConfig;

public class RedisCachePlugin implements ServerCachePlugin {

  /**
   * Create the ServerCacheFactory implementation.
   */
  @Override
  public ServerCacheFactory create(DatabaseConfig config, BackgroundExecutor executor) {
    return new RedisCacheFactory(config, executor);
  }
}
