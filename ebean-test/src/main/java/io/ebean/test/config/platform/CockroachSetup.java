package io.ebean.test.config.platform;

import java.util.Properties;

class CockroachSetup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {

    config.ddlMode("dropCreate");
    config.setDefaultPort(26257);
    config.setUsername("root");
    config.setPassword("");
    config.setUrl("jdbc:postgresql://localhost:${port}/${databaseName}?sslmode=disable");
    config.setDriver("org.postgresql.Driver");
    config.datasourceDefaults();

    return dockerProperties(config);
  }

  private Properties dockerProperties(Config dbConfig) {

    if (!dbConfig.isUseDocker()) {
      return new Properties();
    }

    dbConfig.setDockerVersion("v19.1.3");
    return dbConfig.getDockerProperties();
  }

  @Override
  public void setupExtraDbDataSource(Config config) {
    // not supported yet
  }

  @Override
  public boolean isLocal() {
    return false;
  }

}
