package io.ebeaninternal.server.core;

import io.ebean.DatabaseBuilder;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.DbOffline;

import jakarta.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

/**
 * Create a DatabasePlatform from the configuration.
 * <p>
 * Will used platform name or use the metadata from the JDBC driver to
 * determine the platform automatically.
 */
public class DatabasePlatformFactory {

  private final List<DatabasePlatformProvider> providers = new ArrayList<>();

  public DatabasePlatformFactory() {
    for (DatabasePlatformProvider platformProvider : ServiceLoader.load(DatabasePlatformProvider.class)) {
      providers.add(platformProvider);
    }
  }

  /**
   * Create the appropriate database specific platform.
   */
  public DatabasePlatform create(DatabaseBuilder.Settings config) {
    try {
      String offlinePlatform = DbOffline.getPlatform();
      if (offlinePlatform != null) {
        CoreLog.log.log(INFO, "offline platform [{0}]", offlinePlatform);
        return byDatabaseName(offlinePlatform);
      }
      if (config.getDatabasePlatformName() != null) {
        // choose based on dbName
        return byDatabaseName(config.getDatabasePlatformName());
      }
      if (config.getDataSourceConfig().isOffline()) {
        throw new PersistenceException("DatabasePlatformName must be specified with offline mode");
      }
      // guess using meta data from driver
      return byDataSource(config.getDataSource());

    } catch (Exception ex) {
      throw new PersistenceException(ex);
    }
  }

  /**
   * Lookup the platform by name.
   */
  private DatabasePlatform byDatabaseName(String dbName) {
    dbName = dbName.toLowerCase();
    for (DatabasePlatformProvider provider : providers) {
      if (provider.match(dbName)) {
        return provider.create(dbName);
      }
    }
    throw new RuntimeException("database platform " + dbName + " is not known?");
  }

  /**
   * Use JDBC DatabaseMetaData to determine the platform.
   */
  private DatabasePlatform byDataSource(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {
      DatabasePlatform platform = byDatabaseMeta(connection.getMetaData(), connection);
      if (!connection.getAutoCommit()) {
        // we must roll back before close.
        connection.rollback();
      }
      return platform;
    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }

  /**
   * Find the platform by the metaData.getDatabaseProductName().
   */
  private DatabasePlatform byDatabaseMeta(DatabaseMetaData metaData, Connection connection) throws SQLException {
    String dbProductName = metaData.getDatabaseProductName().toLowerCase();
    final int majorVersion = metaData.getDatabaseMajorVersion();
    final int minorVersion = metaData.getDatabaseMinorVersion();
    CoreLog.log.log(DEBUG, "platform for productName[{0}] version[{1}.{2}]", dbProductName, majorVersion, minorVersion);
    for (DatabasePlatformProvider provider : providers) {
      if (provider.matchByProductName(dbProductName)) {
        return provider.create(majorVersion, minorVersion, metaData, connection);
      }
    }
    if (providers.isEmpty()) {
      throw new IllegalStateException("There are no ebean platform providers in the classpath. " +
        "Add a missing dependency like ebean-h2, ebean-postgres, ebean-mysql etc to support the database [" + dbProductName
        + "]. Adding a dependency on io.ebean:ebean will add support for all platforms.");
    }
    throw new IllegalStateException("Unable to determine the appropriate ebean platform given database product name [" + dbProductName
      + "] and ebean platform providers " + providers + ". With ebean 13+ we now have separate platforms (ebean-postgres, ebean-mysql etc)"
      + " and should use database specific platform dependency like ebean-postgres. Note that we can use ebean-platform-all to include"
      + " all the platforms.");
  }

}
