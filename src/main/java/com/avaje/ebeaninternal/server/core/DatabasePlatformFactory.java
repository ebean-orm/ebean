package com.avaje.ebeaninternal.server.core;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebean.config.dbplatform.HsqldbPlatform;
import com.avaje.ebean.config.dbplatform.MsSqlServer2000Platform;
import com.avaje.ebean.config.dbplatform.MsSqlServer2005Platform;
import com.avaje.ebean.config.dbplatform.MySqlPlatform;
import com.avaje.ebean.config.dbplatform.Oracle10Platform;
import com.avaje.ebean.config.dbplatform.Oracle9Platform;
import com.avaje.ebean.config.dbplatform.PostgresPlatform;
import com.avaje.ebean.config.dbplatform.SQLitePlatform;
import com.avaje.ebean.config.dbplatform.SqlAnywherePlatform;

/**
 * Create a DatabasePlatform from the configuration.
 * <p>
 * Will used platform name or use the meta data from the JDBC driver to
 * determine the platform automatically.
 * </p>
 */
public class DatabasePlatformFactory {

  private static final Logger logger = Logger.getLogger(DatabasePlatformFactory.class.getName());

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
    if (dbName.equals("postgres83")) {
      return new PostgresPlatform();
    }
    if (dbName.equals("oracle9")) {
      return new Oracle9Platform();
    }
    if (dbName.equals("oracle10")) {
      return new Oracle10Platform();
    }
    if (dbName.equals("oracle")) {
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
        logger.log(Level.SEVERE, null, ex);
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

    if (dbProductName.indexOf("oracle") > -1) {
      if (majorVersion > 9) {
        return new Oracle10Platform();
      } else {
        return new Oracle9Platform();
      }
    }
    if (dbProductName.indexOf("microsoft") > -1) {
      if (majorVersion > 8) {
        return new MsSqlServer2005Platform();
      } else {
        return new MsSqlServer2000Platform();
      }
    }

    if (dbProductName.indexOf("mysql") > -1) {
      return new MySqlPlatform();
    }
    if (dbProductName.indexOf("h2") > -1) {
      return new H2Platform();
    }
    if (dbProductName.indexOf("hsql database engine") > -1) {
      return new HsqldbPlatform();
    }
    if (dbProductName.indexOf("postgres") > -1) {
      return new PostgresPlatform();
    }
    if (dbProductName.indexOf("sqlite") > -1) {
      return new SQLitePlatform();
    }
    if (dbProductName.indexOf("sql anywhere") > -1) {
      return new SqlAnywherePlatform();
    }
    
    // use the standard one
    return new DatabasePlatform();
  }
}
