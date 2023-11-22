package io.ebeaninternal.dbmigration;

import io.avaje.classpath.scanner.Resource;
import io.avaje.classpath.scanner.core.Scanner;
import io.ebean.config.ClassLoadConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.migration.JdbcMigration;
import io.ebean.migration.MigrationVersion;
import io.ebean.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Generate a migration index file.
 * <p>
 * This is a file that has all the migrations listed in order with checksum of the file content.
 */
class IndexMigration {

  private static final String eol = "\n";
  private final List<Entry> all = new ArrayList<>();
  private final File topDir;

  private final String javaMigrations;
  private final DatabasePlatform databasePlatform;
  private final File indexFile;
  private final Stack<String> pathStack = new Stack<>();

  private final ClassLoadConfig classLoadConfig;

  IndexMigration(File topDir, String javaMigrations, DatabasePlatform databasePlatform, ClassLoadConfig classLoadConfig) {
    this.topDir = topDir;
    this.javaMigrations = javaMigrations.replace('/', '.');
    this.databasePlatform = databasePlatform;
    this.classLoadConfig = classLoadConfig;
    this.indexFile = init();
  }

  IndexMigration(File topDir, String javaMigrations, DefaultDbMigration.Pair pair, ClassLoadConfig classLoadConfig) {
    this.classLoadConfig = classLoadConfig;
    this.topDir = new File(topDir, pair.prefix);
    this.javaMigrations = javaMigrations == null ? null : javaMigrations.replace('/', '.') + "." + pair.prefix;
    this.databasePlatform = pair.platform;
    this.indexFile = init();
  }

  File init() {
    pathStack.push("");
    String name = "idx_" + databasePlatform.platform().base().name().toLowerCase() + ".migrations";
    return new File(topDir, name);
  }

  void generate() throws IOException {
    readSqlFiles(topDir);
    if (javaMigrations != null && classLoadConfig != null) {
      readJdbcMigrations(javaMigrations);
    }
    generateIndex();
  }

  private void generateIndex() throws IOException {
    Collections.sort(all);
    try (Writer writer = IOUtils.newWriter(indexFile)) {
      for (Entry entry : all) {
        writeChecksumPadded(writer, entry.checksum);
        writer.write(entry.fileName);
        writer.write(eol);
      }
      writer.write(eol);
    }
  }

  private void writeChecksumPadded(Writer writer, int checksum) throws IOException {
    final String asStr = String.valueOf(checksum);
    writer.write(asStr);
    writer.write(',');
    int max = 15 - asStr.length();
    for (int i = 0; i < max; i++) {
      writer.write(' ');
    }
  }

  private void readSqlFiles(File dir) {
    final File[] files = dir.listFiles();
    if (files != null && files.length > 0) {
      for (File file : files) {
        if (file.isDirectory()) {
          readDirectory(file);
        }
        final String lowerName = file.getName().toLowerCase();
        if (lowerName.endsWith(".sql")) {
          addEntry(file);
        }
      }
    }
  }

  private void readJdbcMigrations(String pkg) {
    List<Resource> jdbcResources = new Scanner(classLoadConfig.getClassLoader()).scanForResources(pkg, name -> name.endsWith(".class") && !name.contains("$"));
    for (Resource jdbcResource : jdbcResources) {
      String fileName = jdbcResource.name();
      int pos = fileName.lastIndexOf(".class");
      String mainName = fileName.substring(0, pos);
      MigrationVersion migrationVersion = MigrationVersion.parse(mainName);

      String className = jdbcResource.location().replace('/', '.');
      className = className.substring(0, className.length() - 6);
      JdbcMigration instance = (JdbcMigration) classLoadConfig.newInstance(className);

      final int checksum = instance.getChecksum();
      all.add(new Entry(checksum, migrationVersion,  mainName));
    }

  }

  private void readDirectory(File dir) {
    final String current = pathStack.peek();
    pathStack.push(current + dir.getName() + "/");
    readSqlFiles(dir);
    // FIXME
    pathStack.pop();
  }

  private void addEntry(File sqlFile) {
    final String relativePath = pathStack.peek();
    final String fileName = sqlFile.getName();
    final String name = fileName.substring(0, fileName.length() - 4);
    final MigrationVersion version = MigrationVersion.parse(name);
    final int checksum = MChecksum.calculate(sqlFile);
    all.add(new Entry(checksum, version, relativePath + fileName));
  }

  static class Entry implements Comparable<Entry> {

    private final int checksum;
    private final String fileName;
    private final MigrationVersion version;

    Entry(int checksum, MigrationVersion version, String fileName) {
      this.checksum = checksum;
      this.version = version;
      this.fileName = fileName;
    }

    @Override
    public int compareTo(Entry other) {
      return version.compareTo(other.version);
    }
  }

}
