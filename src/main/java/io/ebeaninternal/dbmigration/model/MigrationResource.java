package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.migration.Migration;
import io.ebeaninternal.dbmigration.migrationreader.MigrationXmlReader;

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
  public MigrationResource(File migrationFile, MigrationVersion version) {
    this.migrationFile = migrationFile;
    this.version = version;
  }

  @Override
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
