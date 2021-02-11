package io.ebeaninternal.server.core;

import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.clickhouse.ClickHousePlatform;
import io.ebean.config.dbplatform.cockroach.CockroachPlatform;
import io.ebean.config.dbplatform.db2.DB2Platform;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.hana.HanaPlatform;
import io.ebean.config.dbplatform.hsqldb.HsqldbPlatform;
import io.ebean.config.dbplatform.mariadb.MariaDbPlatform;
import io.ebean.config.dbplatform.mysql.MySql55Platform;
import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import io.ebean.config.dbplatform.nuodb.NuoDbPlatform;
import io.ebean.config.dbplatform.oracle.Oracle11Platform;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import io.ebean.config.dbplatform.postgres.Postgres9Platform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebean.config.dbplatform.sqlanywhere.SqlAnywherePlatform;
import io.ebean.config.dbplatform.sqlite.SQLitePlatform;
import io.ebean.config.dbplatform.sqlserver.SqlServer16Platform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.DbOffline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
  public DatabasePlatform create(DatabaseConfig config) {
    try {
      String offlinePlatform = DbOffline.getPlatform();
      if (offlinePlatform != null) {
        logger.info("offline platform [{}]", offlinePlatform);
        return byDatabaseName(offlinePlatform);
      }

      if (config.getDatabasePlatformName() != null) {
        // choose based on dbName
        return byDatabaseName(config.getDatabasePlatformName());
      }

      if (config.getDataSourceConfig().isOffline()) {
        throw new PersistenceException("You must specify a DatabasePlatformName when you are offline");
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
    if (dbName.equals("h2")) {
      return new H2Platform();
    }
    if (dbName.equals("mariadb")) {
      return new MariaDbPlatform();
    }
    if (dbName.equals("mysql")) {
      return new MySqlPlatform();
    }
    if (dbName.equals("mysql55")) {
      return new MySql55Platform();
    }
    if (dbName.equals("postgres") || dbName.equals("postgres9")) {
      return new PostgresPlatform();
    }
    if (dbName.equals("oracle11") || dbName.equals("oracle10") || dbName.equals("oracle9")) {
      return new Oracle11Platform();
    }
    if (dbName.equals("oracle")) {
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
    if (dbName.equals("clickhouse")) {
      return new ClickHousePlatform();
    }
    if (dbName.equals("nuodb")) {
      return new NuoDbPlatform();
    }
    if (dbName.equals("sqlite")) {
      return new SQLitePlatform();
    }
    if (dbName.equals("hana")) {
      return new HanaPlatform();
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

    if (dbProductName.contains("oracle")) {
      return oracleVersion(majorVersion);
    } else if (dbProductName.contains("microsoft")) {
      throw new IllegalArgumentException("For SqlServer please explicitly choose either sqlserver16 or sqlserver17 as the platform via DatabaseConfig.setDatabasePlatformName. Refer to issue #1340 for more details");
    } else if (dbProductName.contains("h2")) {
      return new H2Platform();
    } else if (dbProductName.contains("hsql database engine")) {
      return new HsqldbPlatform();
    } else if (dbProductName.contains("postgres")) {
      return readPostgres(connection, majorVersion);
    } else if (dbProductName.contains("mariadb")) {
      return new MariaDbPlatform();
    } else if (dbProductName.contains("mysql")) {
      return mysqlVersion(majorVersion, minorVersion);
    } else if (dbProductName.contains("nuo")) {
      return new NuoDbPlatform();
    } else if (dbProductName.contains("sqlite")) {
      return new SQLitePlatform();
    } else if (dbProductName.contains("db2")) {
      return new DB2Platform();
    } else if (dbProductName.contains("sql anywhere")) {
      return new SqlAnywherePlatform();
    } else if (dbProductName.contains("hdb")) {
      return new HanaPlatform();
    } else if (dbProductName.contains("clickhouse")) {
      return new ClickHousePlatform();
    }

    // use the standard one
    return new DatabasePlatform();
  }

  private DatabasePlatform oracleVersion(int majorVersion) {
    return majorVersion < 12 ? new Oracle11Platform() : new OraclePlatform();
  }

  private DatabasePlatform mysqlVersion(int majorVersion, int minorVersion) {
    if (majorVersion <= 5 && minorVersion <= 5) {
      return new MySql55Platform();
    }
    return new MySqlPlatform();
  }

  /**
   * Use a select version() query as it could be Postgres or CockroachDB.
   */
  private static DatabasePlatform readPostgres(Connection connection, int majorVersion) {
    try (PreparedStatement statement = connection.prepareStatement("select version() as \"version\"")) {
      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          String productVersion = resultSet.getString("version").toLowerCase();
          if (productVersion.contains("cockroach")) {
            return new CockroachPlatform();
          }
        }
      }
    } catch (SQLException e) {
      logger.warn("Error running detection query on Postgres", e);
    }

    if (majorVersion <= 9) {
      return new Postgres9Platform();
    }
    return new PostgresPlatform();
  }
}
