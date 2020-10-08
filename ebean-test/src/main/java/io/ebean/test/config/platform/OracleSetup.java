package io.ebean.test.config.platform;

import java.util.Properties;

class OracleSetup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {

    config.ddlMode("dropCreate");
    config.setDefaultPort(1521);
    config.setUsernameDefault();
    config.setPasswordDefault();
    config.setDatabaseName("XE");
    config.setUrl("jdbc:oracle:thin:@localhost:${port}:${databaseName}");
    config.setDriver("oracle.jdbc.driver.OracleDriver");
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
