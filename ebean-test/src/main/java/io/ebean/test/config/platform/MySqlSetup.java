package io.ebean.test.config.platform;

import java.util.Properties;

class MySqlSetup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {

    int defaultPort = config.isUseDocker() ? 4306 : 3306;

    config.ddlMode("dropCreate");
    config.setDefaultPort(defaultPort);
    config.setUsernameDefault();
    config.setPasswordDefault();
    config.setUrl("jdbc:mysql://localhost:${port}/${databaseName}");
    config.setDriver(defaultDriver());
    config.datasourceDefaults();

    return dockerProperties(config);
  }

  private String defaultDriver() {
    try {
      String newDriver = "com.mysql.cj.jdbc.Driver";
      Class.forName(newDriver);
      return newDriver;
    } catch (ClassNotFoundException e) {
      return "com.mysql.jdbc.Driver";
    }
  }

  private Properties dockerProperties(Config dbConfig) {

    if (!dbConfig.isUseDocker()) {
      return new Properties();
    }

    dbConfig.setDockerVersion("8.0");
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
