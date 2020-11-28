package io.ebean.test.config.platform;

import io.ebean.docker.commands.ElasticConfig;
import io.ebean.docker.commands.ElasticContainer;

import java.util.Properties;

/**
 * Setup and start a Docker container for ElasticSearch.
 */
class ElasticSearchSetup {

  private static final String[] DOCKER_PARAMS = {"containerName", "image", "internalPort", "startMode", "shutdown"};

  private final Properties config;

  ElasticSearchSetup(Properties config) {
    this.config = config;
  }

  void run() {
    ElasticConfig elasticConfig = readConfig();
    if (elasticConfig != null) {
      new ElasticContainer(elasticConfig).start();
    }
  }

  ElasticConfig readConfig() {

    String version = read("version", null);
    if (version == null) {
      // we need an explicit version to run
      return null;
    }

    return new ElasticConfig(version, populateDockerProperties(version));
  }

  private Properties populateDockerProperties(String version) {

    PropertiesBuilder properties = new PropertiesBuilder();

    String mode = config.getProperty("ebean.test.shutdown");
    if (mode != null) {
      properties.set("shutdown", mode);
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

    private Properties dockerProperties = new Properties();

    private void set(String key, String val) {
      dockerProperties.setProperty("elastic." + key, val);
    }

    private Properties build() {
      return dockerProperties;
    }
  }

}
