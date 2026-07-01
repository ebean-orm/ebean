package io.ebean.redis;

import io.ebean.BackgroundExecutor;
import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheType;
import io.ebean.redis.encode.EncodeSerializable;
import redis.clients.jedis.util.Pool;
import redis.clients.jedis.Jedis;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Shared fixtures for Redis cache integration tests.
 */
final class RedisTestFixtures {

  private RedisTestFixtures() {
  }

  static String cacheKey(String suffix) {
    return "ebean-redis-it:" + suffix + ":" + System.nanoTime();
  }

  static ServerCacheOptions defaultOptions() {
    return new ServerCacheOptions();
  }

  static ServerCacheConfig naturalKeyConfig(String cacheKey) {
    return cacheConfig(ServerCacheType.NATURAL_KEY, cacheKey, defaultOptions());
  }

  static ServerCacheConfig beanCacheConfig(String cacheKey) {
    return cacheConfig(ServerCacheType.BEAN, cacheKey, defaultOptions());
  }

  static ServerCacheConfig collectionIdsConfig(String cacheKey) {
    return cacheConfig(ServerCacheType.COLLECTION_IDS, cacheKey, defaultOptions());
  }

  static ServerCacheConfig queryCacheConfig(String cacheKey) {
    return cacheConfig(ServerCacheType.QUERY, cacheKey, defaultOptions());
  }

  static ServerCacheConfig nearBeanCacheConfig(String cacheKey) {
    ServerCacheOptions options = defaultOptions();
    options.setNearCache(true);
    return cacheConfig(ServerCacheType.BEAN, cacheKey, options);
  }

  static ServerCacheConfig cacheConfig(ServerCacheType type, String cacheKey, ServerCacheOptions options) {
    return new ServerCacheConfig(type, cacheKey, "testCache", options, null, null);
  }

  static RedisCache naturalKeyCache(Pool<Jedis> pool, String cacheKey) {
    return new RedisCache(pool, naturalKeyConfig(cacheKey), new EncodeSerializable());
  }

  static DatabaseBuilder.Settings databaseSettings(RedisConfig.Mode mode, Pool<Jedis> pool) {
    return Database.builder()
      .putServiceObject(pool)
      .loadFromProperties(RedisLocalTestSupport.propertiesForMode(mode))
      .settings();
  }

  static BackgroundExecutor backgroundExecutor() {
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread thread = new Thread(r, "ebean-redis-test-bg");
      thread.setDaemon(true);
      return thread;
    });
    return new BackgroundExecutor() {
      @Override
      public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
      }

      @Override
      public Future<?> submit(Runnable task) {
        return executor.submit(task);
      }

      @Override
      public void execute(Runnable task) {
        executor.execute(task);
      }

      @Override
      public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        return executor.scheduleWithFixedDelay(task, initialDelay, delay, unit);
      }

      @Override
      public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return executor.scheduleAtFixedRate(task, initialDelay, period, unit);
      }

      @Override
      public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return executor.schedule(task, delay, unit);
      }

      @Override
      public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit unit) {
        return executor.schedule(task, delay, unit);
      }
    };
  }
}
