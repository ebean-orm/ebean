package io.ebean.redis;

import java.util.List;
import java.util.Set;

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
 * Integration tests against a locally deployed Redis (standalone and sentinel).
 * <p>
 * Requires {@code src/test/resources/redis-local.yml} — copy from
 * {@code redis-local.yml.example}. Tests are skipped when the file is
 * missing or Redis is unreachable.
 * </p>
 */
@Tag("redis-local")
class RedisConnectionITest {

  @BeforeEach
  void assumeLocalRedisConfigured() {
    assumeTrue(RedisLocalTestSupport.isConfigured());
  }

  @Nested
  @Disabled("standalone")
  class Standalone {

    private RedisConfig config;
    private Pool<Jedis> pool;

    @BeforeEach
    void connect() {
      config = RedisLocalTestSupport.standaloneConfig();
      assumeTrue(RedisLocalTestSupport.isReachable(config),
        "Skip: standalone Redis not reachable at "
          + config.getServer() + ":" + config.getPort());
      pool = config.createPool();
    }

    @AfterEach
    void close() {
      if (pool != null) {
        pool.close();
      }
    }

    @Test
    void ping() {
      try (Jedis jedis = pool.getResource()) {
        assertThat(jedis.ping()).isEqualToIgnoringCase("PONG");
      }
    }

    @Test
    void setGetDel() {
      String key = RedisLocalTestSupport.testKey("standalone");
      try (Jedis jedis = pool.getResource()) {
        assertThat(jedis.set(key, "value")).isEqualTo("OK");
        assertThat(jedis.get(key)).isEqualTo("value");
        assertThat(jedis.del(key)).isEqualTo(1L);
        assertThat(jedis.get(key)).isNull();
      }
    }

    @Test
    void mget() {
      String key1 = RedisLocalTestSupport.testKey("standalone-a");
      String key2 = RedisLocalTestSupport.testKey("standalone-b");
      try (Jedis jedis = pool.getResource()) {
        jedis.set(key1, "a");
        jedis.set(key2, "b");
        List<String> values = jedis.mget(key1, key2);
        assertThat(values).containsExactly("a", "b");
        jedis.del(key1, key2);
      }
    }

    @Test
    void publish() {
      try (Jedis jedis = pool.getResource()) {
        assertThat(jedis.publish("ebean-redis-it:channel", "hello")).isGreaterThanOrEqualTo(0L);
      }
    }

    @Test
    void loadFromYamlConfig() {
      RedisConfig reloaded = RedisLocalTestSupport.standaloneConfig();
      assertThat(reloaded.getMode()).isEqualTo(RedisConfig.Mode.STANDALONE);
      assertThat(reloaded.getServer()).isEqualTo(config.getServer());
      assertThat(reloaded.getPort()).isEqualTo(config.getPort());
      assertThat(reloaded.getPassword()).isEqualTo(config.getPassword());
      assertThat(reloaded.getMaxTotal()).isEqualTo(config.getMaxTotal());
      assertThat(RedisLocalTestSupport.isReachable(reloaded)).isTrue();
    }
  }

  @Nested
  @Disabled("Sentinel")
  class Sentinel {

    private RedisConfig config;
    private Pool<Jedis> pool;

    @BeforeEach
    void connect() {
      config = RedisLocalTestSupport.sentinelConfig();
      assumeTrue(!config.getSentinels().isEmpty(),
        "Skip: ebean.redis.sentinels not configured in redis-local.yml");
      assumeTrue(RedisLocalTestSupport.isReachable(config),
        "Skip: sentinel Redis not reachable, master="
          + config.getMasterName() + " sentinels=" + config.getSentinels());
      pool = config.createPool();
    }

    @AfterEach
    void close() {
      if (pool != null) {
        pool.close();
      }
    }

    @Test
    void ping() {
      try (Jedis jedis = pool.getResource()) {
        assertThat(jedis.ping()).isEqualToIgnoringCase("PONG");
      }
    }

    @Test
    void connectsToMaster() {
      try (Jedis jedis = pool.getResource()) {
        String role = jedis.info("replication").lines()
          .filter(line -> line.startsWith("role:"))
          .findFirst()
          .orElse("");
        assertThat(role).contains("master");
      }
    }

    @Test
    void setGetDel() {
      String key = RedisLocalTestSupport.testKey("sentinel");
      try (Jedis jedis = pool.getResource()) {
        assertThat(jedis.set(key, "sentinel-value")).isEqualTo("OK");
        assertThat(jedis.get(key)).isEqualTo("sentinel-value");
        assertThat(jedis.del(key)).isEqualTo(1L);
      }
    }

    @Test
    void mget() {
      String key1 = RedisLocalTestSupport.testKey("sentinel-a");
      String key2 = RedisLocalTestSupport.testKey("sentinel-b");
      try (Jedis jedis = pool.getResource()) {
        jedis.set(key1, "1");
        jedis.set(key2, "2");
        assertThat(jedis.mget(key1, key2)).containsExactly("1", "2");
        jedis.del(key1, key2);
      }
    }

    @Test
    void publish() {
      try (Jedis jedis = pool.getResource()) {
        assertThat(jedis.publish("ebean-redis-it:sentinel-channel", "hello")).isGreaterThanOrEqualTo(0L);
      }
    }

    @Test
    void loadFromYamlConfig() {
      RedisConfig reloaded = RedisLocalTestSupport.sentinelConfig();
      assertThat(reloaded.getMode()).isEqualTo(RedisConfig.Mode.SENTINEL);
      assertThat(reloaded.getMasterName()).isEqualTo(config.getMasterName());
      assertThat(reloaded.getSentinels()).isEqualTo(Set.copyOf(config.getSentinels()));
      assertThat(reloaded.getPassword()).isEqualTo(config.getPassword());
      assertThat(reloaded.getMaxTotal()).isEqualTo(config.getMaxTotal());
      assertThat(RedisLocalTestSupport.isReachable(reloaded)).isTrue();
    }
  }
}
