package com.avaje.ebean.dbmigration.runner;

import com.avaje.ebean.dbmigration.model.MigrationVersion;
import org.avaje.classpath.scanner.Resource;

/**
 * A DB migration resource (DDL script with version).
 */
public class LocalMigrationResource implements Comparable<LocalMigrationResource> {

  /**
   * Code for repeatable migrations.
   */
  private static final String REPEAT_TYPE = "R";

  /**
   * Code for version migrations.
   */
  private static final String VERSION_TYPE = "V";

  private final MigrationVersion version;

  private final String location;

  private final Resource resource;

  /**
   * Construct with version and resource.
   */
  public LocalMigrationResource(MigrationVersion version, String location, Resource resource) {
    this.version = version;
    this.location = location;
    this.resource = resource;
  }

  public String toString() {
    return version.toString();
  }

  /**
   * Return true if the underlying version is "repeatable".
   */
  public boolean isRepeatable() {
    return version.isRepeatable();
  }

  /**
   * Return the "key" that identifies the migration.
   */
  public String key() {
    if (isRepeatable()) {
      return version.getComment().toLowerCase();
    } else {
      return version.normalised();
    }
  }

  /**
   * Return the migration comment.
   */
  public String getComment() {
    String comment = version.getComment();
    return (comment == null || comment.isEmpty()) ? "-" : comment;
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

  /**
   * Return the type code ("R" or "V") for this migration.
   */
  public String getType() {
    return isRepeatable() ? REPEAT_TYPE : VERSION_TYPE;
  }
}
