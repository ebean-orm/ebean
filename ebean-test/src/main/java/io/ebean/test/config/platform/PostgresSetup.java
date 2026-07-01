package io.ebean.test.config.platform;

import java.util.Properties;

class PostgresSetup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {
    int defaultPort = config.isUseDocker() ? 6432 : 5432;
    config.ddlMode("dropCreate");
    config.setDefaultPort(defaultPort);
    config.setUsernameDefault();
    config.setPasswordDefault();
    config.setUrl("jdbc:postgresql://${host}:${port}/${databaseName}");
    String schema = config.getSchema();
    if (schema != null && !schema.equals(config.getUsername())) {
      config.urlAppend("?currentSchema=" + schema);
    }
    config.setDriver("org.postgresql.Driver");
    config.datasourceDefaults();
    return dockerProperties(config);
  }

  private Properties dockerProperties(Config config) {
    if (!config.isUseDocker()) {
      return new Properties();
    }
    config.setDockerVersion("16");
    config.setExtensions("hstore,pgcrypto");
    return config.getDockerProperties();
  }

  @Override
  public void setupExtraDbDataSource(Config config) {
    int defaultPort = config.isUseDocker() ? 6432 : 5432;
    config.setDefaultPort(defaultPort);
    config.setExtraUsernameDefault();
    config.setExtraDbPasswordDefault();
    config.setUrl("jdbc:postgresql://${host}:${port}/${databaseName}");
    config.setDriver("org.postgresql.Driver");
    config.extraDatasourceDefaults();
  }

  @Override
  public boolean isLocal() {
    return false;
  }

}
