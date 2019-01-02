package io.ebeaninternal.dbmigration;

import io.ebeaninternal.dbmigration.model.MigrationVersion;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    List<String> fileNames = new ArrayList<>();

    File[] sqlFiles = migDirectory.listFiles(pathname -> includeSqlFile(pathname.getName().toLowerCase()));
    if (sqlFiles != null) {
      for (File file : sqlFiles) {
        fileNames.add(trim(file.getName()));
      }
    }

    if (modelDir != null) {
      File[] xmlFiles = modelDir.listFiles(pathname -> includeModelFile(pathname.getName().toLowerCase()));
      if (xmlFiles != null) {
        for (File file : xmlFiles) {
          fileNames.add(trim(file.getName()));
        }
      }
    }

    Collections.sort(fileNames);
    if (!fileNames.isEmpty()) {
      return fileNames.get(fileNames.size() - 1);
    }
    return null;
  }

  private static boolean includeSqlFile(String lowerFileName) {
    if (lowerFileName.startsWith("r") || lowerFileName.startsWith("i") || !lowerFileName.endsWith(SQL)) {
      return false;
    }
    return true;
  }

  private static boolean includeModelFile(String lowerFileName) {
    return lowerFileName.endsWith(MODEL_XML);
  }

  private static String trim(String name) {
    name = name.toLowerCase();
    char c = name.charAt(0);
    if (c == 'v') {
      name = name.substring(1);
    }
    int p = name.indexOf("__");
    if (p > -1) {
      name = name.substring(0, p);
    }
    if (name.endsWith(SQL)) {
      name = name.substring(0, name.length() - 4);
    }
    if (name.endsWith(MODEL_XML)) {
      name = name.substring(0, name.length() - 10);
    }
    return name;
  }

}
