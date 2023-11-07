package io.ebean.test.config;

import io.avaje.applog.AppLog;
import io.ebean.config.AutoConfigure;
import io.ebean.DatabaseBuilder;
import io.ebean.datasource.DataSourceBuilder;
import io.ebean.test.config.platform.PlatformAutoConfig;
import io.ebean.test.config.provider.ProviderAutoConfig;
import io.ebean.test.containers.DockerHost;

import java.util.Properties;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

/**
 * Automatically configure ServerConfig for testing purposes.
 * <p>
 * Can setup and execute docker based databases and other containers.
 * Can setup DataSource configuration (to match docker db setup).
 * Can setup a CurrentUserProvider and CurrentTenantProvider for testing.
 * Can setup a EncryptKeyManager for testing purposes with fixed key.
 */
public class AutoConfigureForTesting implements AutoConfigure {

  private static final System.Logger log = AppLog.getLogger("io.ebean.test");

  /**
   * System property that can override the platform.  mvn clean test -Ddb=sqlserver
   */
  private final String environmentDb = System.getProperty("db");

  @Override
  public void preConfigure(DatabaseBuilder builder) {
    var config = builder.settings();
    Properties properties = config.getProperties();
    if (properties != null) {
      // trigger determination of docker.host system property if not already done
      // and re-evaluate properties in case there is use of ${docker.host} in jdbc url etc
      DockerHost.host();
      io.avaje.config.Config.asConfiguration().evalModify(properties);
    }
    if (!config.isDefaultServer()) {
      log.log(INFO, "skip automatic testing config on non-default server name:{0} register:{1}", config.getName(), config.isRegister());
      return;
    }
    if (isExtraServer(config, properties)) {
      setupExtraDataSourceIfNecessary(config);
      return;
    }
    String testPlatform = properties.getProperty("ebean.test.platform");
    log.log(DEBUG, "automatic testing config - with ebean.test.platform:{0} name:{1} environmentDb:{2}", testPlatform, config.getName(), environmentDb);
    if (RunOnceMarker.isRun()) {
      setupPlatform(environmentDb, config);
    }
  }

  @Override
  public void postConfigure(DatabaseBuilder builder) {
    var config = builder.settings();
    if (!config.isDefaultServer()) {
      return;
    }
    setupProviders(config);
    if (org.h2.engine.Constants.VERSION_MAJOR == 1) {
      // This code may be removed later, when droppinv H2 1.xxx compatibility
      System.err.println("Running tests in H2 1.xxx compatibility mode");
      System.setProperty("ebean.h2.useV1Syntax", "true");
      makeV1Compatible(config.getDataSourceConfig());
      makeV1Compatible(config.getReadOnlyDataSourceConfig());
    }
  }

  private void makeV1Compatible(DataSourceBuilder.Settings ds) {
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
    ds.url(url);
  }

  /**
   * Check if this is not the primary server and return true if that is the case.
   */
  private boolean isExtraServer(DatabaseBuilder.Settings config, Properties properties) {
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
  private void setupExtraDataSourceIfNecessary(DatabaseBuilder.Settings config) {
    DataSourceBuilder dataSourceConfig = config.getDataSourceConfig();
    if (dataSourceConfig == null || dataSourceConfig.settings().getUsername() == null) {
      new PlatformAutoConfig(environmentDb, config)
        .configExtraDataSource();
    }
  }

  /**
   * Setup support for Who, Multi-Tenant and DB encryption if they are not already set.
   */
  private void setupProviders(DatabaseBuilder.Settings config) {
    new ProviderAutoConfig(config).run();
  }

  /**
   * Setup the platform for testing including docker as needed and adjusting datasource config as needed.
   */
  private void setupPlatform(String db, DatabaseBuilder.Settings config) {
    new PlatformAutoConfig(db, config).run();
  }
}
