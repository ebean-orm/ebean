package io.ebean.test.config.platform;

import java.util.Properties;

final class PGvectorSetup implements PlatformSetup {

  @Override
  public Properties setup(Config config) {
    int defaultPort = config.isUseDocker() ? 8432 : 5432;
    config.setDockerPlatform("pgvector");
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
    config.setExtensions("vector");
    config.setDockerContainerName("ut_pgvector");
    config.setDockerVersion("pg18");
    return config.getDockerProperties();
  }

  @Override
  public void setupExtraDbDataSource(Config config) {
    int defaultPort = config.isUseDocker() ? 8432 : 5432;
    config.setDefaultPort(defaultPort);
    config.setExtraUsernameDefault();
    config.setExtraDbPasswordDefault();
    config.setExtraUrl("jdbc:postgresql://${host}:${port}/${databaseName}");
    config.extraDatasourceDefaults();
  }

  @Override
  public boolean isLocal() {
    return false;
  }
}
