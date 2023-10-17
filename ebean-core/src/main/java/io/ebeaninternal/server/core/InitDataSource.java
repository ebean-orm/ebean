package io.ebeaninternal.server.core;

import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.datasource.DataSourceAlertFactory;
import io.ebean.datasource.DataSourceConfig;
import io.ebean.datasource.DataSourceFactory;
import io.ebean.datasource.DataSourcePoolListener;

import jakarta.persistence.PersistenceException;
import javax.sql.DataSource;

/**
 * Initialise the main DataSource and read-only DataSource.
 */
final class InitDataSource {

  private final DatabaseConfig config;

  /**
   * Create and set the main DataSource and read-only DataSource.
   */
  static void init(DatabaseConfig config) {
    new InitDataSource(config).initialise();
  }

  InitDataSource(DatabaseConfig config) {
    this.config = config;
  }

  private void initialise() {
    if (config.getDataSource() == null) {
      config.setDataSource(initDataSource());
    }
    if (config.getReadOnlyDataSource() == null) {
      config.setReadOnlyDataSource(initReadOnlyDataSource());
    }
  }

  /**
   * Initialise the "main" read write DataSource from configuration.
   */
  private DataSource initDataSource() {
    return createFromConfig(config.getDataSourceConfig(), false);
  }

  /**
   * Initialise the "read only" DataSource from configuration.
   */
  private DataSource initReadOnlyDataSource() {
    DataSourceConfig roConfig = readOnlyConfig();
    return roConfig == null ? null : createFromConfig(roConfig, true);
  }

  DataSourceConfig readOnlyConfig() {
    DataSourceConfig roConfig = config.getReadOnlyDataSourceConfig();
    if (roConfig == null) {
      // it has explicitly been set to null, not expected but ok
      return null;
    }
    if (urlSet(roConfig.getUrl())) {
      return roConfig;
    }
    // convenient alternate place to set the read-only url
    final String readOnlyUrl = config.getDataSourceConfig().getReadOnlyUrl();
    if (urlSet(readOnlyUrl)) {
      roConfig.setUrl(readOnlyUrl);
      return roConfig;
    }
    if (config.isAutoReadOnlyDataSource()) {
      roConfig.setUrl(null); // blank out in case it is "none"
      return roConfig;
    } else {
      return null;
    }
  }

  private boolean urlSet(String url) {
    return url != null && !"none".equalsIgnoreCase(url) && !url.trim().isEmpty();
  }

  private DataSource createFromConfig(DataSourceConfig dsConfig, boolean readOnly) {
    if (dsConfig == null) {
      throw new PersistenceException("No DataSourceConfig defined for " + config.getName());
    }
    if (dsConfig.isOffline()) {
      if (config.getDatabasePlatformName() == null) {
        throw new PersistenceException("You MUST specify a DatabasePlatformName on DatabaseConfig when offline");
      }
    }

    attachAlert(dsConfig);
    attachListener(dsConfig);

    if (readOnly) {
      // setup to use AutoCommit such that we skip explicit commit
      dsConfig.setAutoCommit(true);
      dsConfig.setReadOnly(true);
      dsConfig.setDefaults(config.getDataSourceConfig());
      dsConfig.setIsolationLevel(config.getDataSourceConfig().getIsolationLevel());
    } else if (isPostgresAllQuotedIdentifiers()) {
      dsConfig.addProperty("quoteReturningIdentifiers", false);
    }
    return create(dsConfig, readOnly);
  }

  boolean isPostgresAllQuotedIdentifiers() {
    return config.isAllQuotedIdentifiers() && Platform.POSTGRES == config.getDatabasePlatform().platform().base();
  }

  private DataSource create(DataSourceConfig dsConfig, boolean readOnly) {
    String poolName = config.getName() + (readOnly ? "-ro" : "");
    return DataSourceFactory.create(poolName, dsConfig);
  }

  /**
   * Attach DataSourceAlert via service loader if present.
   */
  private void attachAlert(DataSourceConfig dsConfig) {
    DataSourceAlertFactory alertFactory = config.getServiceObject(DataSourceAlertFactory.class);
    if (alertFactory == null) {
      alertFactory = ServiceUtil.service(DataSourceAlertFactory.class);
    }
    if (alertFactory != null) {
      dsConfig.setAlert(alertFactory.createAlert());
    }
  }

  /**
   * Create and attach a DataSourcePoolListener if it has been specified via properties and there is not one already attached.
   */
  private void attachListener(DataSourceConfig dsConfig) {
    if (dsConfig.getListener() == null) {
      String poolListener = dsConfig.getPoolListener();
      if (poolListener != null) {
        dsConfig.setListener((DataSourcePoolListener) config.getClassLoadConfig().newInstance(poolListener));
      }
    }
  }
}
