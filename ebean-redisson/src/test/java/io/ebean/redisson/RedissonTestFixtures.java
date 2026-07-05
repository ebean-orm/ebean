package io.ebean.redisson;

import io.ebean.BackgroundExecutor;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheType;
import io.ebean.redisson.encode.SerializableCodec;
import org.jspecify.annotations.NonNull;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

final class RedissonTestFixtures {

  private RedissonTestFixtures() {}

  static String cacheKey(String suffix) {
    return "ebean-redisson-it:" + suffix + ":" + System.nanoTime();
  }

  static ServerCacheOptions defaultOptions() {
    return new ServerCacheOptions();
  }

  static ServerCacheOptions ttlOptions(int secsToLive) {
    ServerCacheOptions opts = new ServerCacheOptions();
    opts.setMaxSecsToLive(secsToLive);
    return opts;
  }

  static ServerCacheOptions idleOptions(int maxIdleSecs) {
    ServerCacheOptions opts = new ServerCacheOptions();
    opts.setMaxIdleSecs(maxIdleSecs);
    return opts;
  }

  static ServerCacheOptions maxSizeOptions(int maxSize) {
    ServerCacheOptions opts = new ServerCacheOptions();
    opts.setMaxSize(maxSize);
    return opts;
  }

  static ServerCacheConfig cacheConfig(ServerCacheType type, String cacheKey, ServerCacheOptions options) {
    return new ServerCacheConfig(type, cacheKey, "testCache", options, null, null);
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

  static ServerCacheConfig nearNaturalKeyConfig(String cacheKey) {
    ServerCacheOptions opts = defaultOptions();
    opts.setNearCache(true);
    return cacheConfig(ServerCacheType.NATURAL_KEY, cacheKey, opts);
  }

  static ServerCacheConfig nearBeanCacheConfig(String cacheKey) {
    ServerCacheOptions opts = defaultOptions();
    opts.setNearCache(true);
    return cacheConfig(ServerCacheType.BEAN, cacheKey, opts);
  }

  static RedissonCache naturalKeyCache(RedissonClient client, String cacheKey) {
    return new RedissonCache(client, naturalKeyConfig(cacheKey), new SerializableCodec(), null, false);
  }

  static RedissonCache beanCache(RedissonClient client, String cacheKey) {
    return new RedissonCache(client, beanCacheConfig(cacheKey), new SerializableCodec(), null, false);
  }

  static DatabaseBuilder.Settings databaseSettings(RedissonClient client) {
    return Database.builder()
      .name("redisson-factory-test")
      .putServiceObject(client)
      .settings();
  }

  static RedissonClient createClient() {
    DB.getDefault();
    Config cfg;
    InputStream is = Thread.currentThread().getContextClassLoader()
      .getResourceAsStream("redisson-config.yaml");
    if (is != null) {
      cfg = Config.fromYAML(is);
    } else {
      cfg = new Config();
      cfg.useSingleServer().setAddress("redis://localhost:6379");
    }
    return Redisson.create(cfg);
  }

  static BackgroundExecutor backgroundExecutor() {
    ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "ebean-redisson-test-bg");
      t.setDaemon(true);
      return t;
    });
    return new BackgroundExecutor() {
      @Override public <T> Future<T> submit(Callable<T> task) { return ex.submit(task); }
      @Override public Future<?> submit(Runnable task) { return ex.submit(task); }
      @Override public void execute(Runnable task) { ex.execute(task); }
      @Override public ScheduledFuture<?> scheduleWithFixedDelay(@NonNull Runnable t, long i, long d, @NonNull TimeUnit u) { return ex.scheduleWithFixedDelay(t, i, d, u); }
      @Override public ScheduledFuture<?> scheduleAtFixedRate(@NonNull Runnable t, long i, long p, @NonNull TimeUnit u) { return ex.scheduleAtFixedRate(t, i, p, u); }
      @Override public ScheduledFuture<?> schedule(@NonNull Runnable t, long d, @NonNull TimeUnit u) { return ex.schedule(t, d, u); }
      @Override public <V> ScheduledFuture<V> schedule(@NonNull Callable<V> t, long d, @NonNull TimeUnit u) { return ex.schedule(t, d, u); }
    };
  }
}
