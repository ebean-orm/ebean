package io.ebean.platform.postgres;

import io.ebean.annotation.Platform;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;
import io.ebean.platform.cockroach.CockroachPlatform;
import io.ebean.platform.yugabyte.YugabytePlatform;

import javax.persistence.PersistenceException;
import java.sql.*;
import java.util.Locale;

/**
 * Postgres, Yugabyte and Cockroach platform provider.
 */
public class PostgresPlatformProvider implements DatabasePlatformProvider {

  @Override
  public String toString() {
    return "Postgres,Cockroach,Yugabyte";
  }

  @Override
  public boolean match(String name) {
    return name.startsWith("postgres") || name.equals("cockroach") || name.equals("yugabyte");
  }

  @Override
  public DatabasePlatform create(String name) {
    switch (name) {
      case "postgres9":
        return new Postgres9Platform();
      case "postgres":
        return new PostgresPlatform();
      case "cockroach":
        return new CockroachPlatform();
      case "yugabyte":
        return new YugabytePlatform();
    }
    throw new IllegalArgumentException("Unknown platform name " + name);
  }

  @Override
  public boolean matchByProductName(String productName) {
    return productName.contains("postgres");
  }

  @Override
  public DatabasePlatform create(int majorVersion, int minorVersion, DatabaseMetaData meta, Connection connection) {
    try {
      String productVersion = meta.getDatabaseProductVersion().toLowerCase(Locale.ENGLISH);
      if (productVersion.contains("-yb-")) {
        return new YugabytePlatform();
      }
      try (Statement statement = connection.createStatement()) {
        try (ResultSet resultSet = statement.executeQuery("select version()")) {
          if (resultSet.next()) {
            productVersion = resultSet.getString(1).toLowerCase();
            if (productVersion.contains("cockroach")) {
              return new CockroachPlatform();
            }
          }
        }
      } finally {
        if (!connection.getAutoCommit()) {
          connection.rollback();
        }
      }
      if (majorVersion <= 9) {
        return new Postgres9Platform();
      }
      return new PostgresPlatform();
    } catch (SQLException e) {
      throw new PersistenceException("Error trying to determine postgres platform via JDBC metadata", e);
    }
  }

  @Override
  public boolean matchPlatform(Platform platform) {
    switch (platform) {
      case POSTGRES:
      case POSTGRES9:
      case COCKROACH:
      case YUGABYTE:
        return true;
    }
    return false;
  }

  @Override
  public DatabasePlatform create(Platform platform) {
    switch (platform) {
      case POSTGRES:
        return new PostgresPlatform();
      case POSTGRES9:
        return new Postgres9Platform();
      case COCKROACH:
        return new CockroachPlatform();
      case YUGABYTE:
        return new YugabytePlatform();
    }
    throw new IllegalArgumentException("Unknown platform " + platform);
  }
}
