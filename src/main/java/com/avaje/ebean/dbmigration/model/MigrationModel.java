package com.avaje.ebean.dbmigration.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Build the model from the series of migrations.
 */
public class MigrationModel {

  private static final Logger logger = LoggerFactory.getLogger(MigrationModel.class);

  private final ModelContainer model = new ModelContainer();

  private final File modelDirectory;

  private final String modelSuffix;

  private MigrationVersion lastVersion;

  public MigrationModel(File modelDirectory, String modelSuffix) {
    this.modelDirectory = modelDirectory;
    this.modelSuffix = modelSuffix;
  }

  /**
   * Read all the migrations returning the model with all
   * the migrations applied in version order.
   */
  public ModelContainer read() {

    readMigrations();
    return model;
  }

  private void readMigrations() {

    // find all the migration xml files
    File[] xmlFiles = modelDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith(modelSuffix);
      }
    });

    List<MigrationResource> resources = new ArrayList<MigrationResource>();

    for (File xmlFile: xmlFiles) {
      resources.add(new MigrationResource(xmlFile));
    }

    // sort into version order before applying
    Collections.sort(resources);

    for (MigrationResource migrationResource: resources) {
      logger.debug("read {}", migrationResource);
      model.apply(migrationResource.read());
    }

    // remember the last version
    if (!resources.isEmpty()) {
      lastVersion = resources.get(resources.size() - 1).getVersion();
    }
  }

  public String getNextVersion(String initialVersion) {

    return lastVersion == null ? initialVersion : lastVersion.nextVersion();
  }
}
