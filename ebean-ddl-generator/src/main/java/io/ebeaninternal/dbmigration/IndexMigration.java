package io.ebeaninternal.dbmigration;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.migration.MigrationVersion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
  private final DatabasePlatform databasePlatform;
  private final File indexFile;
  private final Stack<String> pathStack = new Stack<>();

  IndexMigration(File topDir, DatabasePlatform databasePlatform) {
    this.topDir = topDir;
    this.databasePlatform = databasePlatform;
    this.indexFile = init();
  }

  IndexMigration(File topDir, DefaultDbMigration.Pair pair) {
    this.topDir = new File(topDir, pair.prefix);
    this.databasePlatform = pair.platform;
    this.indexFile = init();
  }

  File init() {
    pathStack.push("");
    String name = "idx_" + databasePlatform.getPlatform().base().name().toLowerCase() + ".migrations";
    return new File(topDir, name);
  }

  void generate() throws IOException {
    readSqlFiles(topDir);
    generateIndex();
  }

  private void generateIndex() throws IOException {
    Collections.sort(all);
    FileWriter writer = new FileWriter(indexFile);
    for (Entry entry : all) {
      writeChecksumPadded(writer, entry.checksum);
      writer.write(entry.fileName);
      writer.write(eol);
    }
    writer.write(eol);
    writer.close();
  }

  private void writeChecksumPadded(FileWriter writer, int checksum) throws IOException {
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

  private void readDirectory(File dir) {
    final String current = pathStack.peek();
    pathStack.push(current + dir.getName() + "/");
    readSqlFiles(dir);
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
