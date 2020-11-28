package io.ebean.test.config.platform;

import java.util.Properties;

class ClickHouseSetup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {

    config.ddlMode("dropCreate");
    config.setDefaultPort(8123);
    config.setUsername("default");
    config.setPassword("");
    config.setUrl("jdbc:clickhouse://localhost:${port}/${databaseName}");
    config.setDriver("ru.yandex.clickhouse.ClickHouseDriver");
    config.datasourceDefaults();

    return dockerProperties(config);
  }

  private Properties dockerProperties(Config dbConfig) {

    if (!dbConfig.isUseDocker()) {
      return new Properties();
    }

    dbConfig.setDockerVersion("latest");
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
