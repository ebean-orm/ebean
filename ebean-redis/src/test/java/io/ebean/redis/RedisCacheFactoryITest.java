package io.ebean.redis;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import io.ebean.DatabaseBuilder;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheNotification;
import io.ebean.cache.ServerCacheNotify;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for {@link RedisCacheFactory} against local standalone and sentinel Redis.
 */
@Tag("redis-local")
class RedisCacheFactoryITest {

  @BeforeEach
  void assumeLocalRedisConfigured() {
    assumeTrue(RedisLocalTestSupport.isConfigured());
  }

  abstract static class FactoryTestCase {

    RedisConfig config;
    Pool<Jedis> pool;
    RedisCacheFactory factory;

    @BeforeEach
    void setUp() {
      config = redisConfig();
      assumeTrue(RedisLocalTestSupport.isReachable(config),
        "Skip: Redis not reachable for " + modeLabel());
      pool = config.createPool();
      DatabaseBuilder.Settings settings = RedisTestFixtures.databaseSettings(mode(), pool);
      factory = new RedisCacheFactory(settings, RedisTestFixtures.backgroundExecutor());
    }

    @AfterEach
    void tearDown() {
      if (pool != null) {
        pool.close();
      }
    }

    abstract RedisConfig redisConfig();

    abstract RedisConfig.Mode mode();

    abstract String modeLabel();

    @Test
    void createsNaturalKeyCache_roundTrip() {
      String cacheKey = RedisTestFixtures.cacheKey("factory-nk");
      ServerCache cache = factory.createCache(RedisTestFixtures.naturalKeyConfig(cacheKey));
      assertThat(cache).isInstanceOf(RedisCache.class);

      cache.put("1", "one");
      assertThat(cache.get("1")).isEqualTo("one");

      RedisCache redisCache = (RedisCache) cache;
      assertThat(redisCache.getHitCount()).isEqualTo(1);
      cache.clear();
    }

    @Test
    void createsBeanCache() {
      String cacheKey = RedisTestFixtures.cacheKey("factory-bean");
      ServerCache cache = factory.createCache(RedisTestFixtures.beanCacheConfig(cacheKey));
      assertThat(cache).isInstanceOf(RedisCache.class);
    }

    @Test
    void createsCollectionIdsCache() {
      String cacheKey = RedisTestFixtures.cacheKey("factory-coll");
      ServerCache cache = factory.createCache(RedisTestFixtures.collectionIdsConfig(cacheKey));
      assertThat(cache).isInstanceOf(RedisCache.class);
    }

    @Test
    void createsNearCache_asDuelCache() {
      String cacheKey = RedisTestFixtures.cacheKey("factory-near");
      ServerCache cache = factory.createCache(RedisTestFixtures.nearBeanCacheConfig(cacheKey));
      assertThat(cache).isInstanceOf(DuelCache.class);

      cache.put("1", "near");
      assertThat(cache.get("1")).isEqualTo("near");
      cache.clear();
    }

    @Test
    void queryCache_isSingletonPerKey() {
      String cacheKey = RedisTestFixtures.cacheKey("factory-query");
      ServerCache first = factory.createCache(RedisTestFixtures.queryCacheConfig(cacheKey));
      ServerCache second = factory.createCache(RedisTestFixtures.queryCacheConfig(cacheKey));
      assertThat(first).isSameAs(second);
    }

    @Test
    void queryCacheClear_doesNotThrow() {
      String cacheKey = RedisTestFixtures.cacheKey("factory-query-clear");
      ServerCache cache = factory.createCache(RedisTestFixtures.queryCacheConfig(cacheKey));
      assertNotNull(cache);
      cache.clear();
    }

    @Test
    void cacheNotify_publishTableMod_doesNotThrow() {
      ServerCacheNotify notify = factory.createCacheNotify(n -> {
      });
      assertNotNull(notify);
      notify.notify(new ServerCacheNotification(Set.of("foo", "bar")));
    }

    @Test
    void cacheNotify_tableMod_notifiesOtherFactory() throws InterruptedException {
      CopyOnWriteArrayList<ServerCacheNotification> notifications = new CopyOnWriteArrayList<>();
      DatabaseBuilder.Settings settings = RedisTestFixtures.databaseSettings(mode(), pool);
      RedisCacheFactory otherFactory = new RedisCacheFactory(settings, RedisTestFixtures.backgroundExecutor());
      otherFactory.createCacheNotify(notifications::add);

      // allow redis-sub thread to connect
      Thread.sleep(300);

      ServerCacheNotify notify = factory.createCacheNotify(n -> {
      });
      notify.notify(new ServerCacheNotification(Set.of("foo", "bar")));

      Thread.sleep(500);
      assertThat(notifications).isNotEmpty();
      assertThat(notifications.get(0).getDependentTables()).contains("foo", "bar");
    }

    @Test
    void cacheNotify_emptyTables_doesNotThrow() {
      ServerCacheNotify notify = factory.createCacheNotify(n -> {
      });

      assertThat(notify).isNotNull();
      notify.notify(new ServerCacheNotification(Set.of()));
    }

    @Test
    void usesInjectedPool() {
      try (Jedis jedis = pool.getResource()) {
        String marker = RedisTestFixtures.cacheKey("factory-pool");
        jedis.set(marker, "ok");
        assertThat(jedis.get(marker)).isEqualTo("ok");
        jedis.del(marker);
      }
    }
  }

  @Nested
  @Disabled("Standalone")
  class Standalone extends FactoryTestCase {
    @Override
    RedisConfig redisConfig() {
      return RedisLocalTestSupport.standaloneConfig();
    }

    @Override
    RedisConfig.Mode mode() {
      return RedisConfig.Mode.STANDALONE;
    }

    @Override
    String modeLabel() {
      return "standalone";
    }
  }

  @Nested
  @Disabled("Sentinel")
  class Sentinel extends FactoryTestCase {
    @Override
    RedisConfig redisConfig() {
      return RedisLocalTestSupport.sentinelConfig();
    }

    @Override
    RedisConfig.Mode mode() {
      return RedisConfig.Mode.SENTINEL;
    }

    @Override
    String modeLabel() {
      return "sentinel";
    }
  }
}
