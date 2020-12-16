package io.ebean.redis.topic;

import redis.clients.jedis.Jedis;

/**
 * Topic subscriber that has re-connect notification.
 */
public interface DaemonTopic {

  /**
   * Subscribe to the topic/channel (blocking).
   *
   * @param jedis The redis connection to subscribe (and block on).
   */
  void subscribe(Jedis jedis);

  /**
   * Notify that the topic subscription has been connected (or reconnected).
   */
  void notifyConnected();
}
