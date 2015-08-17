package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.dbmigration.migrationreader.MigrationXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Build the model from the series of migrations.
 */
public class MigrationModel {

  private static final Logger logger = LoggerFactory.getLogger(MigrationModel.class);

  private final ModelContainer model = new ModelContainer();

  private final Set<String> readVersions = new LinkedHashSet<String>();

  private final String resourcePath;

  int nextMajorVersion;

  public MigrationModel(String resourcePath) {
    this.resourcePath = normaliseResourcePath(resourcePath);
  }

  private String normaliseResourcePath(String resourcePath) {
    if (resourcePath.endsWith("/")) {
      // trim trailing slash
      resourcePath = resourcePath.substring(0, resourcePath.length()-1);
    }
    if (resourcePath.startsWith("/")) {
      // trim leading slash
      resourcePath = resourcePath.substring(1);
    }
    return resourcePath;
  }

  /**
   * Read all the migrations returning the model with all
   * the migrations applied in version order.
   */
  public ModelContainer read() {

    readMigrations();
    logger.info("read versions {}", readVersions);
    return model;
  }

  /**
   * Return the set of versions that were read.
   */
  public Set<String> getReadVersions() {
    return readVersions;
  }

  public int getNextMajorVersion() {
    return nextMajorVersion;
  }

  private void readMigrations() {

    for (int majorVersion = 1; majorVersion < 100; majorVersion++) {
      if (!readMinorVersions(majorVersion)){
        // no major.0 version so stopping
        nextMajorVersion = majorVersion;
        return;
      }
    }
  }

  private boolean readMinorVersions(int majorVersion) {

    for (int minorVersion = 0; minorVersion < 100; minorVersion++) {
      if (!readMigration(majorVersion, minorVersion)) {
        // continue reading next major if minorVersion 0 was read
        return (minorVersion > 0);
      }
    }
    return true;
  }

  private boolean readMigration(int majorVersion, int minorVersion) {

    String version = majorVersion+"."+minorVersion;
    String path = "/"+resourcePath+"/v"+version+".xml";

    Migration migration = MigrationXmlReader.readMaybe(path);
    if (migration == null) {
      logger.debug("... no migration at path:{}", path);
      return false;
    }
    readVersions.add(version);
    logger.trace("... read migration v{}", version);
    model.apply(migration);
    return true;
  }

}
