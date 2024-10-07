package io.ebean.cache;

import io.ebean.DatabaseBuilder;

/**
 * Plugin that provides a ServerCacheNotify implementation.
 * <p>
 * Is supplied this will be used to send the ServerCacheNotification event to other cluster members.
 */
public interface ServerCacheNotifyPlugin {

  /**
   * Create a ServerCacheNotify implementation given the server configuration.
   */
  ServerCacheNotify create(DatabaseBuilder config);
}
