package io.ebeaninternal.server.core;

import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.db2.DB2Platform;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.hsqldb.HsqldbPlatform;
import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import io.ebean.config.dbplatform.postgres.Postgres8Platform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebean.config.dbplatform.sqlanywhere.SqlAnywherePlatform;
import io.ebean.config.dbplatform.sqlite.SQLitePlatform;
import io.ebean.config.dbplatform.sqlserver.SqlServer16Platform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.dbmigration.DbOffline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

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

      String offlinePlatform = DbOffline.getPlatform();
      if (offlinePlatform != null) {
        logger.info("offline platform [{}]", offlinePlatform);
        return byDatabaseName(offlinePlatform);
      }

      if (serverConfig.getDatabasePlatformName() != null) {
        // choose based on dbName
        return byDatabaseName(serverConfig.getDatabasePlatformName());
      }

      if (serverConfig.getDataSourceConfig().isOffline()) {
        throw new PersistenceException("You must specify a DatabasePlatformName when you are offline");
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
  private DatabasePlatform byDatabaseName(String dbName) {

    dbName = dbName.toLowerCase();
    if (dbName.equals("h2")) {
      return new H2Platform();
    }
    if (dbName.equals("mysql")) {
      return new MySqlPlatform();
    }
    if (dbName.equals("postgres") || dbName.equals("postgres9")) {
      return new PostgresPlatform();
    }
    if (dbName.equals("postgres8") || dbName.equals("postgres83")) {
      return new Postgres8Platform();
    }
    if (dbName.equals("oracle") || dbName.equals("oracle10") || dbName.equals("oracle9")) {
      return new OraclePlatform();
    }
    if (dbName.equals("sqlserver16")) {
      return new SqlServer16Platform();
    }
    if (dbName.equals("sqlserver17")) {
      return new SqlServer17Platform();
    }
    if (dbName.equals("sqlserver")) {
      throw new IllegalArgumentException("Please choose the more specific sqlserver16 or sqlserver17 platform. Refer to issue #1340 for details");
    }
    if (dbName.equals("sqlanywhere")) {
      return new SqlAnywherePlatform();
    }
    if (dbName.equals("db2")) {
      return new DB2Platform();
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

    if (dbProductName.contains("oracle")) {
      return new OraclePlatform();
    } else if (dbProductName.contains("microsoft")) {
      throw new IllegalArgumentException("For SqlServer please explicitly choose either sqlserver16 or sqlserver17 as the platform via ServerConfig.setDatabasePlatformName. Refer to issue #1340 for more details");
    } else if (dbProductName.contains("mysql")) {
      return new MySqlPlatform();
    } else if (dbProductName.contains("h2")) {
      return new H2Platform();
    } else if (dbProductName.contains("hsql database engine")) {
      return new HsqldbPlatform();
    } else if (dbProductName.contains("postgres")) {
      return new PostgresPlatform();
    } else if (dbProductName.contains("sqlite")) {
      return new SQLitePlatform();
    } else if (dbProductName.contains("db2")) {
      return new DB2Platform();
    } else if (dbProductName.contains("sql anywhere")) {
      return new SqlAnywherePlatform();
    }

    // use the standard one
    return new DatabasePlatform();
  }
}
