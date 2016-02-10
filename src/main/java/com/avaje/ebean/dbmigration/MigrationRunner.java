package com.avaje.ebean.dbmigration;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.DbMigrationConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.runner.LocalMigrationResource;
import com.avaje.ebean.dbmigration.runner.LocalMigrationResources;
import com.avaje.ebean.dbmigration.runner.MigrationTable;
import com.avaje.ebeaninternal.util.JdbcClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;

/**
 *
 */
public class MigrationRunner {

  private static final Logger logger = LoggerFactory.getLogger(MigrationRunner.class);

  final EbeanServer server;
  final ServerConfig config;

  private final DbMigrationConfig migrationConfig;

  public MigrationRunner(EbeanServer server) {
    this.server = server;
    this.config = server.getPluginApi().getServerConfig();
    migrationConfig = this.config.getMigrationConfig();
  }

  public void run() {

    LocalMigrationResources resources = new LocalMigrationResources(config);
    if (!resources.readResources()) {
      logger.debug("no migrations to check");
      return;
    }

    Transaction transaction = server.createTransaction();
    Connection connection = transaction.getConnection();
    try {
      MigrationTable table = new MigrationTable(server, migrationConfig, connection);
      table.createIfNeeded();

      LocalMigrationResource priorVersion = null;
      List<LocalMigrationResource> localVersions = resources.getVersions();
      for (int i = 0; i < localVersions.size(); i++) {
        LocalMigrationResource localVersion = localVersions.get(i);
        if (i > 0) {
          priorVersion = localVersions.get(i-1);
        }
        if (!table.shouldRun(localVersion, priorVersion)) {
          break;
        }
      }

      table.commit();

    } catch (Exception e) {
      throw new RuntimeException(e);

    } finally {
      JdbcClose.close(connection);
    }

  }

}
