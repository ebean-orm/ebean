package com.avaje.ebean.dbmigration.runner;

import com.avaje.ebean.dbmigration.model.MigrationVersion;
import org.avaje.classpath.scanner.Resource;

/**
 * A DB migration resource.
 */
public class LocalMigrationResource implements Comparable<LocalMigrationResource> {

  private final MigrationVersion version;

  private final String location;

  private final Resource resource;

  public LocalMigrationResource(MigrationVersion version, String location, Resource resource) {
    this.version = version;
    this.location = location;
    this.resource = resource;
  }

  public String toString() {
    return version.toString();
  }

  /**
   * Default ordering by version.
   */
  @Override
  public int compareTo(LocalMigrationResource o) {
    return version.compareTo(o.version);
  }

  /**
   * Return the underlying migration version.
   */
  public MigrationVersion getVersion() {
    return version;
  }

  /**
   * Return the resource location.
   */
  public String getLocation() {
    return location;
  }

  /**
   * Return the content for the migration apply ddl script.
   */
  public String getContent() {
    return resource.loadAsString("UTF-8");
  }
}
