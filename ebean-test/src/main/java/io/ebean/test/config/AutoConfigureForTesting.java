package io.ebean.test.config;

import io.ebean.config.AutoConfigure;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.test.config.platform.PlatformAutoConfig;
import io.ebean.test.config.provider.ProviderAutoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Automatically configure ServerConfig for testing purposes.
 * <p>
 * Can setup and execute docker based databases and other containers.
 * Can setup DataSource configuration (to match docker db setup).
 * Can setup a CurrentUserProvider and CurrentTenantProvider for testing.
 * Can setup a EncryptKeyManager for testing purposes with fixed key.
 */
public class AutoConfigureForTesting implements AutoConfigure {

  private static final Logger log = LoggerFactory.getLogger("io.ebean.test");

  /**
   * System property that can override the platform.  mvn clean test -Ddb=sqlserver
   */
  private final String environmentDb = System.getProperty("db");

  @Override
  public void preConfigure(DatabaseConfig config) {

    Properties properties = config.getProperties();
    if (isExtraServer(config, properties)) {
      setupExtraDataSourceIfNecessary(config);
      return;
    }

    String testPlatform = properties.getProperty("ebean.test.platform");
    log.debug("automatic testing config - with ebean.test.platform:{} environment db:{} name:{}", testPlatform, environmentDb, config.getName());

    if (RunOnceMarker.isRun()) {
      setupPlatform(environmentDb, config);
    }
  }

  @Override
  public void postConfigure(DatabaseConfig config) {
    setupProviders(config);
    
    if (org.h2.engine.Constants.VERSION_MAJOR == 1) {
      // This code may be removed later, when droppinv H2 1.xxx compatibility
      System.err.println("Running tests in H2 1.xxx compatibility mode");
      System.setProperty("ebean.h2.useV1Syntax", "true");
      makeV1Compatible(config.getDataSourceConfig());
      makeV1Compatible(config.getReadOnlyDataSourceConfig());
    }
  }

  private void makeV1Compatible(DataSourceConfig ds) {
    if (ds == null) {
      return;
    }
    String url = ds.getUrl();
    if (url == null || !url.startsWith("jdbc:h2:")) {
      return;
    }
    // remove illegal URL options
    url = url.replace(";MODE=LEGACY", "");
    url = url.replace(";NON_KEYWORDS=KEY,VALUE", "");
    url = url.replace(";NON_KEYWORDS=KEY", "");
    ds.setUrl(url);
  }

  /**
   * Check if this is not the primary server and return true if that is the case.
   */
  private boolean isExtraServer(DatabaseConfig config, Properties properties) {
    String extraDb = properties.getProperty("ebean.test.extraDb.dbName", properties.getProperty("ebean.test.extraDb"));
    if (extraDb != null && extraDb.equals(config.getName())) {
      config.setDefaultServer(false);
      return true;
    }
    return false;
  }

  /**
   * Setup the DataSource on the extra database if necessary.
   */
  private void setupExtraDataSourceIfNecessary(DatabaseConfig config) {
    DataSourceConfig dataSourceConfig = config.getDataSourceConfig();
    if (dataSourceConfig == null || dataSourceConfig.getUsername() == null) {
      new PlatformAutoConfig(environmentDb, config)
        .configExtraDataSource();
    }
  }

  /**
   * Setup support for Who, Multi-Tenant and DB encryption if they are not already set.
   */
  private void setupProviders(DatabaseConfig config) {
    new ProviderAutoConfig(config).run();
  }

  /**
   * Setup the platform for testing including docker as needed and adjusting datasource config as needed.
   */
  private void setupPlatform(String db, DatabaseConfig config) {
    new PlatformAutoConfig(db, config).run();
  }
}
