package com.avaje.ebean.dbmigration.runner;

import com.avaje.ebean.dbmigration.model.MigrationVersion;
import org.avaje.classpath.scanner.Resource;

/**
 *
 */
public class LocalMigrationResource implements Comparable<LocalMigrationResource> {

  private final MigrationVersion version;

  private final String location;

  private final Resource resource;

  public LocalMigrationResource(MigrationVersion v0, String location, Resource resource) {
    this.version = v0;
    this.location = location;
    this.resource = resource;
  }

  public MigrationVersion getVersion() {
    return version;
  }

  public String getLocation() {
    return location;
  }

  @Override
  public int compareTo(LocalMigrationResource o) {
    return version.compareTo(o.version);
  }
}
