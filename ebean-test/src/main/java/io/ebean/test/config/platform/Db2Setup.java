package io.ebean.test.config.platform;

import java.util.Properties;

class Db2Setup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {

    config.ddlMode("dropCreate");
    config.setDefaultPort(50000);
    config.setUsernameDefault();
    config.setPasswordDefault();
    config.setUrl("jdbc:db2://${host}:${port}/${databaseName}");
    config.setDriver("com.ibm.db2.jcc.DB2Driver");
    config.datasourceDefaults();

    return dockerProperties(config);
  }

  private Properties dockerProperties(Config dbConfig) {

    if (!dbConfig.isUseDocker()) {
      return new Properties();
    }

    dbConfig.setDockerVersion("11.5.9.0");
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
