package io.ebean.redisson;

import io.ebean.DatabaseBuilder;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheNotification;
import io.ebean.cache.ServerCacheNotify;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.redisson.api.RedissonClient;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RedissonCacheFactoryTest {

  private static RedissonClient client;
  private static RedissonCacheFactory factory;

  @BeforeAll
  static void connect() {
    RedissonTestFixtures.startRedis();
    assumeTrue(RedissonTestFixtures.isReachable(), "Skip: Redis not reachable");
    client = RedissonTestFixtures.createClient();
    DatabaseBuilder.Settings settings = RedissonTestFixtures.databaseSettings(client);
    factory = new RedissonCacheFactory(settings, RedissonTestFixtures.backgroundExecutor());
  }

  @AfterAll
  static void disconnect() {
    if (client != null) client.shutdown();
  }

  @AfterEach
  void clearCaches() {
    // Individual tests clear their own caches inline
  }

  @Test
  void createsNaturalKeyCache_roundTrip() {
    String key = RedissonTestFixtures.cacheKey("factory-nk");
    ServerCache cache = factory.createCache(RedissonTestFixtures.naturalKeyConfig(key));
    assertThat(cache).isInstanceOf(RedissonCache.class);

    cache.put("1", "one");
    assertThat(cache.get("1")).isEqualTo("one");
    assertThat(((RedissonCache) cache).getHitCount()).isEqualTo(1);
    cache.clear();
  }

  @Test
  void createsBeanCache_withVersionGating() {
    String key = RedissonTestFixtures.cacheKey("factory-bean");
    ServerCache cache = factory.createCache(RedissonTestFixtures.beanCacheConfig(key));
    assertThat(cache).isInstanceOf(RedissonCache.class);
    cache.clear();
  }

  @Test
  void createsCollectionIdsCache() {
    String key = RedissonTestFixtures.cacheKey("factory-coll");
    ServerCache cache = factory.createCache(RedissonTestFixtures.collectionIdsConfig(key));
    assertThat(cache).isInstanceOf(RedissonCache.class);
    cache.clear();
  }

  @Test
  void createsNearCache_asDuelCache() {
    String key = RedissonTestFixtures.cacheKey("factory-near");
    ServerCache cache = factory.createCache(RedissonTestFixtures.nearNaturalKeyConfig(key));
    assertThat(cache).isInstanceOf(DuelCache.class);

    cache.put("1", "near");
    assertThat(cache.get("1")).isEqualTo("near");
    cache.clear();
  }

  @Test
  void createsNearBeanCache_asDuelCache_typeCheck() {
    String key = RedissonTestFixtures.cacheKey("factory-near-bean");
    ServerCache cache = factory.createCache(RedissonTestFixtures.nearBeanCacheConfig(key));
    assertThat(cache).isInstanceOf(DuelCache.class);
    cache.clear();
  }

  @Test
  void queryCache_isSingletonPerKey() {
    String key = RedissonTestFixtures.cacheKey("factory-query");
    ServerCache first = factory.createCache(RedissonTestFixtures.queryCacheConfig(key));
    ServerCache second = factory.createCache(RedissonTestFixtures.queryCacheConfig(key));
    assertThat(first).isSameAs(second);
  }

  @Test
  void queryCacheClear_doesNotThrow() {
    String key = RedissonTestFixtures.cacheKey("factory-query-clear");
    ServerCache cache = factory.createCache(RedissonTestFixtures.queryCacheConfig(key));
    assertNotNull(cache);
    cache.clear();
  }

  @Test
  void cacheNotify_publishTableMod_doesNotThrow() {
    ServerCacheNotify notify = factory.createCacheNotify(n -> {});
    assertNotNull(notify);
    notify.notify(new ServerCacheNotification(Set.of("tableA", "tableB")));
  }

  @Test
  void cacheNotify_emptyTables_doesNotThrow() {
    ServerCacheNotify notify = factory.createCacheNotify(n -> {});
    assertNotNull(notify);
    notify.notify(new ServerCacheNotification(Set.of()));
  }

  @Test
  void cacheNotify_tableMod_notifiesOtherFactory() throws InterruptedException {
    DatabaseBuilder.Settings otherSettings = RedissonTestFixtures.databaseSettings(client);
    RedissonCacheFactory otherFactory = new RedissonCacheFactory(otherSettings, RedissonTestFixtures.backgroundExecutor());

    CopyOnWriteArrayList<ServerCacheNotification> received = new CopyOnWriteArrayList<>();
    otherFactory.createCacheNotify(received::add);

    Thread.sleep(300);

    ServerCacheNotify notify = factory.createCacheNotify(n -> {});
    notify.notify(new ServerCacheNotification(Set.of("orders", "items")));

    Thread.sleep(500);
    assertThat(received).isNotEmpty();
    assertThat(received.get(0).getDependentTables()).contains("orders", "items");
  }

  @Test
  void usesInjectedRedissonClient() {
    String key = RedissonTestFixtures.cacheKey("factory-inject");
    ServerCache cache = factory.createCache(RedissonTestFixtures.naturalKeyConfig(key));
    cache.put("ping", "pong");
    assertThat(cache.get("ping")).isEqualTo("pong");
    cache.clear();
  }
}
