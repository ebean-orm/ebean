import io.ebean.cache.ServerCachePlugin;

/**
 * Provider of ServerCachePlugin.
 */
module io.ebean.redis {

  provides ServerCachePlugin with io.ebean.redis.RedisCachePlugin;

  requires transitive io.ebean.core;
  requires transitive redis.clients.jedis;

}
