package com.avaje.ebeaninternal.server.core;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a DatabasePlatform from the configuration.
 * <p>
 * Will used platform name or use the meta data from the JDBC driver to
 * determine the platform automatically.
 * </p>
 */
public class DatabasePlatformFactory {

  private static final Logger logger = LoggerFactory.getLogger(DatabasePlatformFactory.class);

  /**
   * Create the appropriate database specific platform.
   */
  public DatabasePlatform create(ServerConfig serverConfig) {

    try {

      if (serverConfig.getDatabasePlatformName() != null) {
        // choose based on dbName
        return byDatabaseName(serverConfig.getDatabasePlatformName());

      }
      if (serverConfig.getDataSourceConfig().isOffline()) {
        String m = "You must specify a DatabasePlatformName when you are offline";
        throw new PersistenceException(m);
      }
      // guess using meta data from driver
      return byDataSource(serverConfig.getDataSource());

    } catch (Exception ex) {
      throw new PersistenceException(ex);
    }
  }

  /**
   * Lookup the platform by name.
   */
  private DatabasePlatform byDatabaseName(String dbName) throws SQLException {

    dbName = dbName.toLowerCase();
    if (dbName.equals("postgres") || dbName.equals("postgres9")) {
      return new PostgresPlatform();
    }
    if (dbName.equals("postgres8") || dbName.equals("postgres83")) {
      return new Postgres8Platform();
    }
    if (dbName.equals("oracle9")) {
      return new Oracle9Platform();
    }
    if (dbName.equals("oracle") || dbName.equals("oracle10")) {
      return new Oracle10Platform();
    }
    if (dbName.equals("sqlserver2005")) {
      return new MsSqlServer2005Platform();
    }
    if (dbName.equals("sqlserver2000")) {
      return new MsSqlServer2000Platform();
    }
    if (dbName.equals("sqlanywhere")) {
      return new SqlAnywherePlatform();
    }
    if (dbName.equals("db2")) {
      return new DB2Platform();
    }
    if (dbName.equals("mysql")) {
      return new MySqlPlatform();
    }

    if (dbName.equals("sqlite")) {
      return new SQLitePlatform();
    }

    throw new RuntimeException("database platform " + dbName + " is not known?");
  }

  /**
   * Use JDBC DatabaseMetaData to determine the platform.
   */
  private DatabasePlatform byDataSource(DataSource dataSource) {

    Connection conn = null;
    try {
      conn = dataSource.getConnection();
      DatabaseMetaData metaData = conn.getMetaData();

      return byDatabaseMeta(metaData);

    } catch (SQLException ex) {
      throw new PersistenceException(ex);

    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex) {
        logger.error(null, ex);
      }
    }
  }

  /**
   * Find the platform by the metaData.getDatabaseProductName().
   */
  private DatabasePlatform byDatabaseMeta(DatabaseMetaData metaData) throws SQLException {

    String dbProductName = metaData.getDatabaseProductName();
    dbProductName = dbProductName.toLowerCase();

    int majorVersion = metaData.getDatabaseMajorVersion();

    if (dbProductName.contains("oracle")) {
      if (majorVersion > 9) {
        return new Oracle10Platform();
      } else {
        return new Oracle9Platform();
      }
    }
    else if (dbProductName.contains("microsoft")) {
      if (majorVersion > 8) {
        return new MsSqlServer2005Platform();
      } else {
        return new MsSqlServer2000Platform();
      }
    }
    else if (dbProductName.contains("mysql")) {
      return new MySqlPlatform();
    }
    else if (dbProductName.contains("h2")) {
      return new H2Platform();
    }
    else if (dbProductName.contains("hsql database engine")) {
      return new HsqldbPlatform();
    }
    else if (dbProductName.contains("postgres")) {
      return new PostgresPlatform();
    }
    else if (dbProductName.contains("sqlite")) {
      return new SQLitePlatform();
    }
    else if (dbProductName.contains("db2")) {
      return new DB2Platform();
    }
    else if (dbProductName.contains("sql anywhere")) {
      return new SqlAnywherePlatform();
    }

      // use the standard one
    return new DatabasePlatform();
  }
}
