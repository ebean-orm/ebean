package com.avaje.ebean.dbmigration.runner;

import com.avaje.ebean.config.DbMigrationConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.model.MigrationVersion;
import org.avaje.classpath.scanner.Location;
import org.avaje.classpath.scanner.MatchResource;
import org.avaje.classpath.scanner.Resource;
import org.avaje.classpath.scanner.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class LocalMigrationResources {

  private static final Logger logger = LoggerFactory.getLogger(LocalMigrationResources.class);

  private final ServerConfig serverConfig;

  private final DbMigrationConfig migrationConfig;

  private final List<LocalMigrationResource> versions = new ArrayList<LocalMigrationResource>();

  public LocalMigrationResources(ServerConfig serverConfig) {
    this.serverConfig = serverConfig;
    this.migrationConfig = serverConfig.getMigrationConfig();
  }

  public boolean readResources() {

    String migrationPath = migrationConfig.getMigrationPath();

    ClassLoader classLoader = serverConfig.getClassLoadConfig().getClassLoader();

    Scanner scanner = new Scanner(classLoader);
    List<Resource> resourceList = scanner.scanForResources(new Location(migrationPath), new Match(migrationConfig));

    logger.debug("resources: {}", resourceList);

    for (Resource resource : resourceList) {
      String filename = resource.getFilename();
      if (filename.endsWith(migrationConfig.getApplySuffix())) {
        int pos = filename.lastIndexOf(migrationConfig.getApplySuffix());
        String mainName = filename.substring(0, pos);

        MigrationVersion v0 = MigrationVersion.parse(mainName);
        LocalMigrationResource res = new LocalMigrationResource(v0, resource.getLocation(), resource);
        versions.add(res);
      }
    }

    Collections.sort(versions);
    return !versions.isEmpty();
  }

  public List<LocalMigrationResource> getVersions() {
    return versions;
  }


  static class Match implements MatchResource {

    final DbMigrationConfig migrationConfig;

    Match(DbMigrationConfig migrationConfig) {
      this.migrationConfig = migrationConfig;
    }

    @Override
    public boolean isMatch(String name) {

      return name.endsWith(migrationConfig.getApplySuffix())
          || name.endsWith(migrationConfig.getModelSuffix())
          || name.endsWith(migrationConfig.getDropSuffix())
          || name.endsWith(migrationConfig.getRollbackSuffix());

    }
  }
}
