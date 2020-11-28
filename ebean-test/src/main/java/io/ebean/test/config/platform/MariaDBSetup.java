package io.ebean.test.config.platform;

import java.util.Properties;

class MariaDBSetup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {

    int defaultPort = config.isUseDocker() ? 4306 : 3306;

    config.ddlMode("dropCreate");
    config.setDefaultPort(defaultPort);
    config.setUsernameDefault();
    config.setPasswordDefault();
    config.setUrl("jdbc:mariadb://localhost:${port}/${databaseName}?useLegacyDatetimeCode=false");
    config.datasourceDefaults();

    return dockerProperties(config);
  }

  private Properties dockerProperties(Config dbConfig) {
    if (!dbConfig.isUseDocker()) {
      return new Properties();
    }
    dbConfig.setDockerVersion("10");
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
