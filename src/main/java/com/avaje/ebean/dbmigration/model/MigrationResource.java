package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.dbmigration.migrationreader.MigrationXmlReader;

import java.io.File;

/**
 * Migration XML resource that holds the changes to be applied.
 */
public class MigrationResource implements Comparable<MigrationResource> {

  private final File migrationFile;

  private final MigrationVersion version;

  /**
   * Construct with a migration xml file.
   */
  public MigrationResource(File migrationFile) {
    this.migrationFile = migrationFile;
    this.version = MigrationVersion.parse(migrationFile.getName());
  }

  public String toString() {
    return migrationFile.getName();
  }

  /**
   * Return the version associated with this resource.
   */
  public MigrationVersion getVersion() {
    return version;
  }

  /**
   * Read and return the migration from the resource.
   */
  public Migration read() {

    return MigrationXmlReader.read(migrationFile);
  }

  /**
   * Compare by underlying version.
   */
  @Override
  public int compareTo(MigrationResource other) {
    return version.compareTo(other.version);
  }
}
