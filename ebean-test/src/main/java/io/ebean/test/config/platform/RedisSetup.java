package io.ebean.test.config.platform;

import io.ebean.docker.commands.RedisConfig;
import io.ebean.docker.commands.RedisContainer;

import java.util.Properties;

class RedisSetup {

  static void run(Properties properties) {

    String version = properties.getProperty("ebean.test.redis");
    version = properties.getProperty("ebean.test.redis.version", version);
    if (version != null) {
      RedisConfig redisConfig = new RedisConfig(version, properties);
      RedisContainer container = new RedisContainer(redisConfig);
      container.start();
    }
  }
}
