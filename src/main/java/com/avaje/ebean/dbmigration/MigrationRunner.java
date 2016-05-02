package com.avaje.ebean.dbmigration;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.DbMigrationConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.runner.LocalMigrationResource;
import com.avaje.ebean.dbmigration.runner.LocalMigrationResources;
import com.avaje.ebean.dbmigration.runner.MigrationTable;
import com.avaje.ebeaninternal.util.JdbcClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Runs the DB migration typically on application start.
 */
public class MigrationRunner {

  public static final Logger logger = LoggerFactory.getLogger("org.avaje.ebean.DbMigration");

  private final EbeanServer server;

  private final ServerConfig config;

  private final DbMigrationConfig migrationConfig;

  public MigrationRunner(EbeanServer server, DbMigrationConfig migrationConfig) {
    this.server = server;
    this.config = server.getPluginApi().getServerConfig();
    this.migrationConfig = migrationConfig;
  }

  /**
   * Run the migrations if there are any that need running.
   */
  public void run() {

    LocalMigrationResources resources = new LocalMigrationResources(config, migrationConfig);
    if (!resources.readResources()) {
      logger.debug("no migrations to check");
      return;
    }

    String migrationUser = migrationConfig.getDbUser();
    String migrationPwd = migrationConfig.getDbPassword();
    if (migrationUser == null) {
      throw new IllegalStateException("No DB migration user specified (to run the db migration) ?");
    }

    DataSource dataSource = server.getPluginApi().getDataSource();
    if (dataSource == null) {
      throw new IllegalStateException("No dataSource when trying to run migration? "
          +"Maybe trying to generate DBMigration when ebean.migration.run=true is set? "
          +"Perhaps need to set ebean.migration.run=false in test-ebean.properties?");
    }

    Connection connection;
    try {
      connection = dataSource.getConnection(migrationUser, migrationPwd);
    } catch (SQLException e) {
      throw new IllegalArgumentException("Error trying to connect to database using DB Migration user [" + migrationUser + "]", e);
    }

    try {
      logger.debug("using db user [{}] to run migrations ...");
      connection.setAutoCommit(false);
      runMigrations(resources, connection);

      connection.commit();

    } catch (Exception e) {
      JdbcClose.rollback(connection);
      throw new RuntimeException(e);

    } finally {
      JdbcClose.close(connection);
    }
  }

  /**
   * Run all the migrations as needed.
   */
  private void runMigrations(LocalMigrationResources resources, Connection connection) throws SQLException, IOException {

    MigrationTable table = new MigrationTable(server, migrationConfig, connection);
    table.createIfNeeded();

    // get the migrations in version order
    List<LocalMigrationResource> localVersions = resources.getVersions();

    logger.info("local migrations:{}  existing migrations:{}", localVersions.size(), table.size());

    LocalMigrationResource priorVersion = null;

    // run migrations in order
    for (int i = 0; i < localVersions.size(); i++) {
      LocalMigrationResource localVersion = localVersions.get(i);
      if (!table.shouldRun(localVersion, priorVersion)) {
        break;
      }
      priorVersion = localVersion;
      connection.commit();
    }
  }

}
