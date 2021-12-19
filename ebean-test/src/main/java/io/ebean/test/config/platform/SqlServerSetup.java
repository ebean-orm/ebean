package io.ebean.test.config.platform;

import java.util.Properties;

class SqlServerSetup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {

    config.setDatabasePlatformName();

    config.ddlMode("dropCreate");
    config.setDefaultPort(1433);
    config.setUsernameDefault();
    config.setPassword("SqlS3rv#r");
    config.setUrl("jdbc:sqlserver://localhost:${port};databaseName=${databaseName};sendTimeAsDateTime=false");
    config.setDriver("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    config.datasourceDefaults();

    return dockerProperties(config);
  }

  private Properties dockerProperties(Config dbConfig) {

    if (!dbConfig.isUseDocker()) {
      return new Properties();
    }

    dbConfig.setDockerVersion("2019-GA-ubuntu-16.04");
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
