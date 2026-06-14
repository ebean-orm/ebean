package io.ebean.redis;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class RedisLocalYamlTest {

  @Test
  void parseStandaloneDocument() {
    Properties props = RedisLocalTestSupport.parseRedisDocument(
      "ebean:\n"
        + "  redis:\n"
        + "    model: standalone\n"
        + "    server: 192.168.204.140\n"
        + "    port: 6379\n"
        + "    password: secret\n"
    );

    assertThat(props.getProperty("ebean.redis.mode")).isEqualTo("standalone");
    assertThat(props.getProperty("ebean.redis.server")).isEqualTo("192.168.204.140");
    assertThat(props.getProperty("ebean.redis.port")).isEqualTo("6379");
    assertThat(props.getProperty("ebean.redis.password")).isEqualTo("secret");
  }

  @Test
  void parseSentinelDocument() {
    Properties props = RedisLocalTestSupport.parseRedisDocument(
      "ebean:\n"
        + "  redis:\n"
        + "    mode: sentinel\n"
        + "    sentinels: host1:26379,host2:26379\n"
        + "    masterName: mymaster\n"
        + "    password: secret\n"
    );

    assertThat(props.getProperty("ebean.redis.mode")).isEqualTo("sentinel");
    assertThat(props.getProperty("ebean.redis.sentinels")).isEqualTo("host1:26379,host2:26379");
    assertThat(props.getProperty("ebean.redis.masterName")).isEqualTo("mymaster");
  }

  @Test
  void loadDocumentsFromClasspath() {
    if (!RedisLocalTestSupport.isConfigured()) {
      return;
    }
    List<Properties> documents = RedisLocalTestSupport.loadDocuments();
    assertThat(documents).hasSizeGreaterThanOrEqualTo(2);

    RedisConfig standalone = RedisLocalTestSupport.standaloneConfig();
    assertThat(standalone.getMode()).isEqualTo(RedisConfig.Mode.STANDALONE);
    assertThat(standalone.getServer()).isNotBlank();
    assertThat(standalone.getPort()).isPositive();

    RedisConfig sentinel = RedisLocalTestSupport.sentinelConfig();
    assertThat(sentinel.getMode()).isEqualTo(RedisConfig.Mode.SENTINEL);
    assertThat(sentinel.getMasterName()).isEqualTo("mymaster");
    assertThat(sentinel.getSentinels()).hasSize(3);
  }
}
