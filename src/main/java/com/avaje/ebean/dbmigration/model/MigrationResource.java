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
  public MigrationResource(File migrationFile, MigrationVersion version) {
    this.migrationFile = migrationFile;
    this.version = version;
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

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (obj.getClass() == this.getClass()) {
      MigrationResource other = (MigrationResource) obj;
      boolean result = true;
      result &= version != null ? version.equals(other.version) : other.version == null;
      result &= migrationFile != null ? migrationFile.equals(other.migrationFile) : other.migrationFile == null;
      return result;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = 23;
    result = version != null ? 31 * result + version.hashCode() : result;
    result = migrationFile != null ? 31 * result + migrationFile.hashCode() : result;
    return result;
  }
}
