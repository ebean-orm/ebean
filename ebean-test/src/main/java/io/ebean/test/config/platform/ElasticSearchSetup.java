package io.ebean.test.config.platform;

import io.ebean.docker.commands.ElasticContainer;

import java.util.Properties;

/**
 * Setup and start a Docker container for ElasticSearch.
 */
class ElasticSearchSetup {

  private static final String[] DOCKER_PARAMS = {"containerName", "image", "internalPort", "startMode", "shutdownMode"};

  private final Properties config;

  ElasticSearchSetup(Properties config) {
    this.config = config;
  }

  void run() {
    String version = read("version", null);
    if (version != null) {
      Properties properties = populateDockerProperties(version);
      ElasticContainer.builder(version)
        .properties(properties)
        .build()
        .start();
    }
  }

  private Properties populateDockerProperties(String version) {
    PropertiesBuilder properties = new PropertiesBuilder();
    String mode = config.getProperty("ebean.test.shutdownMode");
    if (mode != null) {
      properties.set("shutdownMode", mode);
    }

    properties.set("version", version);
    properties.set("port", read("port", "9201"));
    for (String dockerParam : DOCKER_PARAMS) {
      String val = read(dockerParam, null);
      if (val != null) {
        properties.set(dockerParam, val);
      }
    }

    return properties.build();
  }

  private String read(String key, String defaultValue) {
    return config.getProperty("ebean.docstore.elastic." + key, defaultValue);
  }

  private static class PropertiesBuilder {

    private final Properties dockerProperties = new Properties();

    private void set(String key, String val) {
      dockerProperties.setProperty("elastic." + key, val);
    }

    private Properties build() {
      return dockerProperties;
    }
  }

}
