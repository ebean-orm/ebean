package io.ebean.redis;

import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for {@link RedisConfig}
 **/
class RedisConfigTest {

  @Test
  void loadProperties() {
    Properties p = new Properties();
    p.setProperty("ebean.redis.server", "test-server");
    p.setProperty("ebean.redis.port", "99");
    p.setProperty("ebean.redis.maxIdle", "5");
    p.setProperty("ebean.redis.maxTotal", "6");
    p.setProperty("ebean.redis.minIdle", "7");
    p.setProperty("ebean.redis.maxWaitMillis", "8");
    p.setProperty("ebean.redis.username", "un");
    p.setProperty("ebean.redis.password", "pw");
    p.setProperty("ebean.redis.ssl", "true");

    RedisConfig config = new RedisConfig();
    config.loadProperties(p);

    assertThat(config.getMode()).isEqualTo(RedisConfig.Mode.STANDALONE);
    assertThat(config.getServer()).isEqualTo("test-server");
    assertThat(config.getPort()).isEqualTo(99);
    assertThat(config.getMaxIdle()).isEqualTo(5);
    assertThat(config.getMaxTotal()).isEqualTo(6);
    assertThat(config.getMinIdle()).isEqualTo(7);
    assertThat(config.getMaxWaitMillis()).isEqualTo(8);
    assertThat(config.getUsername()).isEqualTo("un");
    assertThat(config.getPassword()).isEqualTo("pw");
    assertThat(config.isSsl()).isTrue();
  }

  @Test
  void loadSentinelProperties() {
    Properties p = new Properties();
    p.setProperty("ebean.redis.mode", "sentinel");
    p.setProperty("ebean.redis.masterName", "mymaster");
    p.setProperty("ebean.redis.sentinels", "host1:26379, host2:26379 ,host3:26379");
    p.setProperty("ebean.redis.password", "pw");

    RedisConfig config = new RedisConfig();
    config.loadProperties(p);

    assertThat(config.getMode()).isEqualTo(RedisConfig.Mode.SENTINEL);
    assertThat(config.getMasterName()).isEqualTo("mymaster");
    assertThat(config.getSentinels()).containsExactlyInAnyOrder("host1:26379", "host2:26379", "host3:26379");
    assertThat(config.getPassword()).isEqualTo("pw");
  }

  @Test
  void sentinelRequiresMasterName() {
    RedisConfig config = new RedisConfig();
    config.setMode(RedisConfig.Mode.SENTINEL);
    config.setSentinels(Set.of("host1:26379"));

    assertThatThrownBy(config::createPool)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("masterName");
  }

  @Test
  void sentinelRequiresSentinels() {
    RedisConfig config = new RedisConfig();
    config.setMode(RedisConfig.Mode.SENTINEL);
    config.setMasterName("mymaster");

    assertThatThrownBy(config::createPool)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("sentinels");
  }

  @Test
  void parseSentinels_empty() {
    assertThat(RedisConfig.parseSentinels(null)).isEmpty();
    assertThat(RedisConfig.parseSentinels("  ")).isEmpty();
  }

  @Test
  void parseMode_unknown() {
    assertThatThrownBy(() -> RedisConfig.parseMode("cluster"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("cluster");
  }

  @Test
  void test_defaultValues() {
    RedisConfig config = new RedisConfig();
    assertThat(config.getMode()).isEqualTo(RedisConfig.Mode.STANDALONE);
    assertThat(config.getUsername()).isNull();
    assertThat(config.getPassword()).isNull();
    assertThat(config.isSsl()).isFalse();
    assertThat(config.getSentinels()).isEmpty();
  }
}
