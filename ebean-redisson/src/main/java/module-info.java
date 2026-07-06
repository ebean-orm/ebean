import io.ebean.cache.ServerCachePlugin;

/**
 * Provider of ServerCachePlugin.
 */
open module io.ebean.redisson {

  provides ServerCachePlugin with io.ebean.redisson.RedissonCachePlugin;

  requires transitive io.ebean.core;
  requires transitive redisson;
  requires io.netty.buffer;
}
