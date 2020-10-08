package io.ebean.cache;

/**
 * Interface for both listening to notification changes and sending them to other members of the cluster.
 */
public interface ServerCacheNotify {

  /**
   * Notify other server cache members of the table modifications or process the notifications.
   */
  void notify(ServerCacheNotification notification);
}
