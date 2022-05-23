package io.ebean.test.config.platform;

import io.ebean.test.containers.RedisContainer;

import java.util.Properties;

class RedisSetup {

  static void run(Properties properties) {
    String version = properties.getProperty("ebean.test.redis");
    version = properties.getProperty("ebean.test.redis.version", version);
    if (version != null) {
      DockerHost dockerHost = new DockerHost();
      if (dockerHost.runningInDocker()) {
        String host = dockerHost.dockerHost(properties.getProperty("ebean.test.dockerHost"));
        properties.setProperty("redis.host",  host);
      }
      RedisContainer.builder(version)
        .properties(properties)
        .build()
        .start();
    }
  }
}
