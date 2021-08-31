package io.ebean.redis;

import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisConfigTest {

  @Test
  public void loadProperties() {

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

    assertThat(config.getServer()).isEqualTo("test-server");
    assertThat(config.getPort()).isEqualTo(99);
    assertThat(config.getMaxIdle()).isEqualTo(5);
    assertThat(config.getMaxTotal()).isEqualTo(6);
    assertThat(config.getMinIdle()).isEqualTo(7);
    assertThat(config.getMaxWaitMillis()).isEqualTo(8);
    assertThat(config.getUsername()).isEqualTo("un");
    assertThat(config.getPassword()).isEqualTo("pw");
    assertThat(config.isSsl()).isEqualTo(true);
  }

  @Test
  public void test_defaultValues() {
    RedisConfig config = new RedisConfig();
    assertThat(config.getUsername()).isNull();
    assertThat(config.getPassword()).isNull();
    assertThat(config.isSsl()).isEqualTo(false);
  }
}
