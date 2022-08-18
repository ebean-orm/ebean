package io.ebean.redis.topic;

import io.avaje.applog.AppLog;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import java.lang.System.Logger.Level;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Subscribe to redis topic listening for changes.
 * <p>
 * Handles reconnection to redis when the connection is lost and
 * notification of when reconnection takes place.
 * </p>
 */
public final class DaemonTopicRunner {

  private static final System.Logger log = AppLog.getLogger(DaemonTopicRunner.class);

  private static final long reconnectWaitMillis = 1000;

  private final JedisPool jedisPool;
  private final DaemonTopic daemonTopic;

  public DaemonTopicRunner(JedisPool jedisPool, DaemonTopic daemonTopic) {
    this.jedisPool = jedisPool;
    this.daemonTopic = daemonTopic;
  }

  public void run() {
    new Thread(this::attemptConnections, "redis-sub").start();
  }

  private void attemptConnections() {
    Timer reloadTimer = new Timer("redis-sub-notify");
    ReloadNotifyTask notifyTask = null;
    int attempts = 1;
    while (true) {
      if (notifyTask != null) {
        // we didn't successfully re-connect to redis
        notifyTask.cancel();
      }
      notifyTask = new ReloadNotifyTask();
      reloadTimer.schedule(notifyTask, reconnectWaitMillis + 500);
      attempts++;
      try {
        subscribe();
      } catch (JedisException e) {
        log.log(Level.DEBUG, "... redis subscribe connection attempt:{0} failed:{1}", attempts, e.getMessage());
        try {
          // wait a little before retrying
          Thread.sleep(reconnectWaitMillis);
        } catch (InterruptedException e1) {
          Thread.currentThread().interrupt();
          log.log(Level.WARNING, "Interrupted redis re-connection wait", e1);
        }
      }
    }
  }

  /**
   * Subscribe and block when successful.
   */
  private void subscribe() {
    Jedis jedis = jedisPool.getResource();
    jedis.echo("hi");
    try {
      daemonTopic.subscribe(jedis);
    } catch (Exception e) {
      log.log(Level.ERROR, "Lost connection to topic, starting re-connection loop", e);
      attemptConnections();
    } finally {
      try {
        jedis.close();
      } catch (Exception e) {
        log.log(Level.WARNING, "Error closing probably broken Redis connection", e);
      }
    }
  }

  private class ReloadNotifyTask extends TimerTask {
    @Override
    public void run() {
      daemonTopic.notifyConnected();
    }
  }
}
