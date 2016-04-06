package com.avaje.ebean.dbmigration.runner;

import com.avaje.ebean.config.DbMigrationConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.model.MigrationVersion;
import org.avaje.classpath.scanner.Resource;
import org.avaje.classpath.scanner.ResourceFilter;
import org.avaje.classpath.scanner.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads the DB migration resources and sorts them into execution order.
 */
public class LocalMigrationResources {

  private static final Logger logger = LoggerFactory.getLogger(LocalMigrationResources.class);

  private final ServerConfig serverConfig;

  private final DbMigrationConfig migrationConfig;

  private final List<LocalMigrationResource> versions = new ArrayList<LocalMigrationResource>();

  /**
   * Construct with configuration options.
   */
  public LocalMigrationResources(ServerConfig serverConfig, DbMigrationConfig migrationConfig) {
    this.serverConfig = serverConfig;
    this.migrationConfig = migrationConfig;
  }

  /**
   * Read all the migration resources (SQL scripts) returning true if there are versions.
   */
  public boolean readResources() {

    String migrationPath = migrationConfig.getMigrationPath();

    ClassLoader classLoader = serverConfig.getClassLoadConfig().getClassLoader();

    Scanner scanner = new Scanner(classLoader);
    List<Resource> resourceList = scanner.scanForResources(migrationPath, new Match(migrationConfig));

    logger.debug("resources: {}", resourceList);

    for (Resource resource : resourceList) {
      String filename = resource.getFilename();
      if (filename.endsWith(migrationConfig.getApplySuffix())) {
        int pos = filename.lastIndexOf(migrationConfig.getApplySuffix());
        String mainName = filename.substring(0, pos);

        MigrationVersion migrationVersion = MigrationVersion.parse(mainName);
        LocalMigrationResource res = new LocalMigrationResource(migrationVersion, resource.getLocation(), resource);
        versions.add(res);
      }
    }

    Collections.sort(versions);
    return !versions.isEmpty();
  }

  /**
   * Return the list of migration resources in version order.
   */
  public List<LocalMigrationResource> getVersions() {
    return versions;
  }


  /**
   * Filter used to find the migration scripts.
   */
  private static class Match implements ResourceFilter {

    private final DbMigrationConfig migrationConfig;

    Match(DbMigrationConfig migrationConfig) {
      this.migrationConfig = migrationConfig;
    }

    @Override
    public boolean isMatch(String name) {
      return name.endsWith(migrationConfig.getApplySuffix());
    }
  }
}
