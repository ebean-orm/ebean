package io.ebean.cache;

import io.ebean.meta.MetricVisitor;

/**
 * Defines method for constructing caches for beans and queries.
 */
public interface ServerCacheFactory {

  /**
   * Visit the metrics for the cache.
   */
  default void visit(MetricVisitor visitor) {
    // do nothing by default
  }

  /**
   * Create the cache for the given type with options.
   */
  ServerCache createCache(ServerCacheConfig config);

  /**
   * Return a ServerCacheNotify that we will send ServerCacheNotification events to.
   * <p>
   * This is used if a ServerCacheNotifyPlugin is not supplied.
   * </p>
   *
   * @param listener The listener that should be used to process the notification events.
   */
  ServerCacheNotify createCacheNotify(ServerCacheNotify listener);
}
