package io.ebeaninternal.dbmigration.model;

import io.avaje.applog.AppLog;
import io.ebean.migration.MigrationVersion;

import java.io.File;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Build the model from the series of migrations.
 */
public class MigrationModel {

  private static final System.Logger logger = AppLog.getLogger(MigrationModel.class);

  private final ModelContainer model = new ModelContainer();
  private final File modelDirectory;
  private final String modelSuffix;

  public MigrationModel(File modelDirectory, String modelSuffix) {
    this.modelDirectory = modelDirectory;
    this.modelSuffix = modelSuffix;
  }

  /**
   * Read all the migrations returning the model with all
   * the migrations applied in version order.
   *
   * @param initMigration If true we don't apply model changes, migration is from scratch.
   */
  public ModelContainer read(boolean initMigration) {
    readMigrations(initMigration);
    return model;
  }

  private void readMigrations(boolean initMigration) {
    // find all the migration xml files
    File[] xmlFiles = modelDirectory.listFiles(pathname -> pathname.getName().toLowerCase().endsWith(modelSuffix));
    if (xmlFiles == null || xmlFiles.length == 0) {
      return;
    }
    List<MigrationResource> resources = new ArrayList<>(xmlFiles.length);
    for (File xmlFile : xmlFiles) {
      resources.add(new MigrationResource(xmlFile, createVersion(xmlFile)));
    }

    // sort into version order before applying
    Collections.sort(resources);

    if (!initMigration) {
      for (MigrationResource migrationResource : resources) {
        logger.log(Level.DEBUG, "read {0}", migrationResource);
        model.apply(migrationResource.read(), migrationResource.version());
      }
    }
  }

  private MigrationVersion createVersion(File xmlFile) {
    String fileName = xmlFile.getName();
    String versionName = fileName.substring(0, fileName.length() - modelSuffix.length());
    return MigrationVersion.parse(versionName);
  }
}
