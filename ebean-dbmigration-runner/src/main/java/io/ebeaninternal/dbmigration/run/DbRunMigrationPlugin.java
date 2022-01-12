package io.ebeaninternal.dbmigration.run;

import io.ebean.config.DatabaseConfig;
import io.ebean.migration.auto.AutoMigrationRunner;
import io.ebean.plugin.Plugin;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.server.core.ServiceUtil;

public class DbRunMigrationPlugin implements Plugin {

  private SpiServer server;
  private AutoMigrationRunner migrationRunner;

  @Override
  public void configure(SpiServer server) {
    this.server = server;
    DatabaseConfig config = server.config();
    if (config.isRunMigration()) {
      migrationRunner = ServiceUtil.service(AutoMigrationRunner.class);
      if (migrationRunner == null) {
        throw new IllegalStateException(
            "No AutoMigrationRunner found. Probably ebean-migration is not in the classpath?");
      }
      final String dbSchema = config.getDbSchema();
      if (dbSchema != null) {
        migrationRunner.setDefaultDbSchema(dbSchema);
      }
      migrationRunner.setName(config.getName());
      migrationRunner.setPlatform(config.getDatabasePlatform().getPlatform().name().toLowerCase());
      migrationRunner.loadProperties(config.getProperties());
    }
  }

  @Override
  public void online(boolean online) {

  }

  @Override
  public void shutdown() {

  }

  @Override
  public void start() {
    if (migrationRunner != null) {
      migrationRunner.run(server.dataSource());
    }
  }
}
