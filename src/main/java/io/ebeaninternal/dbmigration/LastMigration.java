package io.ebeaninternal.dbmigration;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.ebean.migration.MigrationVersion;

/**
 * Utility to determine the last sql migration version and next version.
 */
class LastMigration {

  private static final String SQL = ".sql";

  private static final String MODEL_XML = ".model.xml";

  /**
   * Return the next migation version given the migration directory.
   */
  static String nextVersion(File migDir, File modelDir, boolean dbinitMigration) {

    String last = lastVersion(migDir, modelDir);
    if (last == null) {
      return null;
    }
    return (dbinitMigration) ? last : MigrationVersion.parse(last).nextVersion();
  }

  /**
   * Return the last migation version given the migration directory.
   */
  static String lastVersion(File migDirectory, File modelDir) {

    List<MigrationVersion> versions = new ArrayList<>();

    File[] sqlFiles = migDirectory.listFiles(pathname -> includeSqlFile(pathname.getName().toLowerCase()));
    if (sqlFiles != null) {
      for (File file : sqlFiles) {
        versions.add(trimAndParse(file.getName()));
      }
    }

    if (modelDir != null) {
      File[] xmlFiles = modelDir.listFiles(pathname -> includeModelFile(pathname.getName().toLowerCase()));
      if (xmlFiles != null) {
        for (File file : xmlFiles) {
          versions.add(trimAndParse(file.getName()));
        }
      }
    }

    Collections.sort(versions);
    if (!versions.isEmpty()) {
      return versions.get(versions.size() - 1).asString();
    }
    return null;
  }

  private static boolean includeSqlFile(String lowerFileName) {
    return !lowerFileName.startsWith("r") && !lowerFileName.startsWith("i") && lowerFileName.endsWith(SQL);
  }

  private static boolean includeModelFile(String lowerFileName) {
    return lowerFileName.endsWith(MODEL_XML);
  }

  private static MigrationVersion trimAndParse(String name) {
    if (name.endsWith(SQL)) {
      name = name.substring(0, name.length() - 4);
    }
    if (name.endsWith(MODEL_XML)) {
      name = name.substring(0, name.length() - 10);
    }
    return MigrationVersion.parse(name);
  }

}
