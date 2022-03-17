package io.ebeaninternal.dbmigration.run;

import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;
import io.ebean.plugin.Plugin;
import io.ebean.plugin.SpiServer;

public class DbRunMigrationPlugin implements Plugin {

  private SpiServer server;
  private MigrationConfig migrationConfig;

  @Override
  public void configure(SpiServer server) {
    this.server = server;
    DatabaseConfig config = server.config();
    if (config.isRunMigration()) {
      migrationConfig = new MigrationConfig();
      final String dbSchema = config.getDbSchema();
      if (dbSchema != null) {
        migrationConfig.setSetCurrentSchema(false);
        migrationConfig.setDbSchema(dbSchema);
      }
      migrationConfig.setName(config.getName());
      Platform platform = config.getDatabasePlatform().getPlatform();
      migrationConfig.setBasePlatform(platform.base().name().toLowerCase());
      migrationConfig.setPlatform(platform.name().toLowerCase());
      migrationConfig.load(config.getProperties());
      migrationConfig.setRunPlaceholderMap(config.getDdlPlaceholderMap());
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
    if (migrationConfig != null) {
      new MigrationRunner(migrationConfig).run(server.dataSource());
    }
  }
}
