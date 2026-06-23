package io.ebean.redis;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.ebean.cache.ServerCacheStatistics;
import io.ebean.cache.ServerCacheType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for {@link RedisCache} against local standalone and sentinel Redis.
 */
@Tag("redis-local")
class RedisCacheITest {

  @BeforeEach
  void assumeLocalRedisConfigured() {
    assumeTrue(RedisLocalTestSupport.isConfigured());
  }

  abstract static class RedisCacheTestCase {

    RedisConfig config;
    Pool<Jedis> pool;
    String cacheKey;
    RedisCache cache;

    @BeforeEach
    void setUp() {
      config = redisConfig();
      assumeTrue(RedisLocalTestSupport.isReachable(config),
        "Skip: Redis not reachable for " + modeLabel());
      pool = config.createPool();
      cacheKey = RedisTestFixtures.cacheKey("RedisCache");
      cache = RedisTestFixtures.naturalKeyCache(pool, cacheKey);
    }

    @AfterEach
    void tearDown() {
      if (cache != null) {
        cache.clear();
      }
      if (pool != null) {
        pool.close();
      }
    }

    abstract RedisConfig redisConfig();

    abstract String modeLabel();

    @Test
    void putAndGet() {
      cache.put("1", "one");
      assertThat(cache.get("1")).isEqualTo("one");
      assertThat(cache.getHitCount()).isEqualTo(1);
      assertThat(cache.getMissCount()).isZero();

      assertThat(cache.get("missing")).isNull();
      assertThat(cache.getMissCount()).isEqualTo(1);
    }

    @Test
    void getAll() {
      cache.put("1", "one");
      cache.put("2", "two");

      Map<Object, Object> found = cache.getAll(Set.of("1", "2", "3"));
      assertThat(found).containsEntry("1", "one").containsEntry("2", "two").doesNotContainKey("3");
      assertThat(cache.getHitCount()).isEqualTo(2);
      assertThat(cache.getMissCount()).isEqualTo(1);
    }

    @Test
    void getAll_emptyKeys() {
      assertThat(cache.getAll(Set.of())).isEmpty();
    }

    @Test
    void putAll() {
      Map<Object, Object> entries = new LinkedHashMap<>();
      entries.put("a", "A");
      entries.put("b", "B");
      cache.putAll(entries);

      assertThat(cache.getAll(Set.of("a", "b")))
        .containsEntry("a", "A")
        .containsEntry("b", "B");
    }

    @Test
    void remove() {
      cache.put("1", "one");
      cache.remove("1");
      assertThat(cache.get("1")).isNull();
    }

    @Test
    void removeAll() {
      cache.put("1", "one");
      cache.put("2", "two");
      cache.removeAll(Set.of("1", "2"));
      assertThat(cache.getAll(Set.of("1", "2"))).isEmpty();
    }

    @Test
    void clear() {
      cache.put("1", "one");
      cache.put("2", "two");
      cache.clear();
      assertThat(cache.getAll(Set.of("1", "2"))).isEmpty();
    }

    @Test
    void statistics() {
      cache.put("1", "one");
      cache.get("1");
      cache.get("missing");
      cache.remove("1");

      ServerCacheStatistics stats = cache.statistics(true);
      assertThat(stats.getCacheName()).isEqualTo(cacheKey);
      assertThat(stats.getHitCount()).isEqualTo(1);
      assertThat(stats.getMissCount()).isEqualTo(1);
      assertThat(stats.getPutCount()).isEqualTo(1);
      assertThat(stats.getRemoveCount()).isEqualTo(1);
    }

    @Test
    void expiration() {
      String ttlCacheKey = RedisTestFixtures.cacheKey("ttl");
      var options = RedisTestFixtures.defaultOptions();
      options.setMaxSecsToLive(3600);
      RedisCache expiringCache = new RedisCache(
        pool,
        RedisTestFixtures.cacheConfig(ServerCacheType.NATURAL_KEY, ttlCacheKey, options),
        new io.ebean.redis.encode.EncodeSerializable()
      );
      try {
        expiringCache.put("k1", "v1");
        try (Jedis jedis = pool.getResource()) {
          byte[] rawKey = (ttlCacheKey + ":k1").getBytes(StandardCharsets.UTF_8);
          assertThat(jedis.ttl(rawKey)).isBetween(1L, 3600L);
        }
      } finally {
        expiringCache.clear();
      }
    }
  }

  @Nested
  @Disabled("Standalone")
  class Standalone extends RedisCacheTestCase {
    @Override
    RedisConfig redisConfig() {
      return RedisLocalTestSupport.standaloneConfig();
    }

    @Override
    String modeLabel() {
      return "standalone";
    }
  }

  @Nested
  @Disabled("Sentinel")
  class Sentinel extends RedisCacheTestCase {
    @Override
    RedisConfig redisConfig() {
      return RedisLocalTestSupport.sentinelConfig();
    }

    @Override
    String modeLabel() {
      return "sentinel";
    }
  }
}
