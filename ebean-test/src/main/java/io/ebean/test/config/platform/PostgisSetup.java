package io.ebean.test.config.platform;

import java.util.Properties;

/**
 * A variation of Postgres that expected Postgis extension support.
 *
 * Uses postgis/postgis image by default.
 */
class PostgisSetup implements PlatformSetup {

  private static final String NET_POSTGIS_DRIVER = "net.postgis.jdbc.DriverWrapperLW";
  private static final String ORG_POSTGIS_DRIVER = "org.postgis.DriverWrapperLW";

  @Override
  public Properties setup(Config config) {
    int defaultPort = config.isUseDocker() ? 7432 : 5432;
    config.setDockerPlatform("postgis");
    config.ddlMode("dropCreate");
    config.setDefaultPort(defaultPort);
    config.setUsernameDefault();
    config.setPasswordDefault();
    config.setDriver(driver(config));
    config.setUrl("jdbc:postgresql_lwgis://${host}:${port}/${databaseName}");
    String schema = config.getSchema();
    if (schema != null && !schema.equals(config.getUsername())) {
      config.urlAppend("?currentSchema=" + schema);
    }
    config.datasourceDefaults();
    return dockerProperties(config);
  }

  private String driver(Config config) {
    String driver = config.getPlatformKey("driver", "");
    if (!driver.isEmpty()) {
      return driver;
    }
    try {
      Class.forName(NET_POSTGIS_DRIVER);
      return NET_POSTGIS_DRIVER;
    } catch (ClassNotFoundException e){
      return ORG_POSTGIS_DRIVER;
    }
  }

  private Properties dockerProperties(Config config) {
    if (!config.isUseDocker()) {
      return new Properties();
    }
    config.setExtensions("hstore,pgcrypto,postgis");
    config.setDockerContainerName("ut_postgis");
    config.setDockerVersion("16-3.5");
    return config.getDockerProperties();
  }

  @Override
  public void setupExtraDbDataSource(Config config) {
    int defaultPort = config.isUseDocker() ? 7432 : 5432;
    config.setDefaultPort(defaultPort);
    config.setExtraUsernameDefault();
    config.setExtraDbPasswordDefault();
    config.setExtraUrl("jdbc:postgresql_lwgis://${host}:${port}/${databaseName}");
    config.extraDatasourceDefaults();
  }

  @Override
  public boolean isLocal() {
    return false;
  }

}
