package io.ebean.test.config.platform;

import io.ebean.datasource.DataSourceConfig;

import java.util.Properties;

class NuoDBSetup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {
    // use testdb as our standard db name and instead use schema
    config.setSchemaFromDbName("testdb");
    config.ddlMode("dropCreate");
    config.setDefaultPort(8888);
    config.setUsernameDefaultSchema();
    config.setPasswordDefault();
    config.setUrl("jdbc:com.nuodb://localhost/testdb");
    config.setDriver("com.nuodb.jdbc.Driver");

    final DataSourceConfig dsConfig = config.datasourceDefaults();
    dsConfig.schema(config.getSchema());
    return dockerProperties(config);
  }

  private Properties dockerProperties(Config dbConfig) {

    if (!dbConfig.isUseDocker()) {
      return new Properties();
    }

    dbConfig.setDockerVersion("4.0.0");
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
