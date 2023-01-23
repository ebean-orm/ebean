package io.ebean.test.config.platform;

import io.ebean.test.containers.DockerHost;
import io.ebean.test.containers.RedisContainer;

import java.util.Properties;

class RedisSetup {

  static void run(Properties properties) {
    String version = properties.getProperty("ebean.test.redis");
    version = properties.getProperty("ebean.test.redis.version", version);
    if (version != null) {
      String host = properties.getProperty("ebean.test.dockerHost", DockerHost.host());
      properties.setProperty("redis.host",  host);
      RedisContainer.builder(version)
        .properties(properties)
        .build()
        .start();
    }
  }
}
