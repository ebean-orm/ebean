package io.ebeaninternal.server.core;

import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.DbOffline;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

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
  public DatabasePlatform create(DatabaseConfig config) {
    try {
      String offlinePlatform = DbOffline.getPlatform();
      if (offlinePlatform != null) {
        CoreLog.log.info("offline platform [{}]", offlinePlatform);
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
    if (dbName.equals("sqlserver")) {
      throw new IllegalArgumentException("Please choose the more specific sqlserver16 or sqlserver17 platform. Refer to issue #1340 for details");
    }
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
      return byDatabaseMeta(connection.getMetaData(), connection);
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
    CoreLog.log.debug("platform for productName[{}] version[{}.{}]", dbProductName, majorVersion, minorVersion);
    if (dbProductName.contains("microsoft")) {
      throw new IllegalArgumentException("For SqlServer please explicitly choose either sqlserver16 or sqlserver17 as the platform via DatabaseConfig.setDatabasePlatformName. Refer to issue #1340 for more details");
    }
    if (dbProductName.contains("postgres")) {
      String productVersion = metaData.getDatabaseProductVersion().toLowerCase(Locale.ENGLISH);
      dbProductName = readPostgres(connection, productVersion);
    }
    for (DatabasePlatformProvider provider : providers) {
      if (provider.matchByProductName(dbProductName)) {
        return provider.create(majorVersion, minorVersion, metaData, connection);
      }
    }
    // use the standard one
    return new DatabasePlatform();
  }

  /**
   * Use a select version() query as it could be Postgres or CockroachDB.
   */
  private static String readPostgres(Connection connection, String productVersion) {
    if (productVersion.contains("-yb-")) {
      return "yugabyte";
    }
    try (PreparedStatement statement = connection.prepareStatement("select version() as \"version\"")) {
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          productVersion = resultSet.getString("version").toLowerCase();
          if (productVersion.contains("cockroach")) {
            return "cockroach";
          }
        }
      }
    } catch (SQLException e) {
      CoreLog.log.warn("Error running detection query on Postgres", e);
    }
    return "postgres";
  }
}
