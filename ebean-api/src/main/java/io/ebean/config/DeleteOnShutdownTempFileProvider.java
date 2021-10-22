package io.ebean.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TempFileProvider implementation, which deletes all temp files on shutdown.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class DeleteOnShutdownTempFileProvider implements TempFileProvider {

  private static final Logger logger = LoggerFactory.getLogger(DeleteOnShutdownTempFileProvider.class);

  List<String> tempFiles = new ArrayList<>();
  private final String prefix;
  private final String suffix;
  private final File directory;

  /**
   * Creates the TempFileProvider with default prefix "db-".
   */
  public DeleteOnShutdownTempFileProvider() {
    this("db-", null, null);
  }

  /**
   * Creates the TempFileProvider.
   */
  public DeleteOnShutdownTempFileProvider(String prefix, String suffix, File directory) {
    this.prefix = prefix;
    this.suffix = suffix;
    this.directory = directory;
  }

  @Override
  public File createTempFile() throws IOException {
    File file = File.createTempFile(prefix, suffix, directory);
    synchronized (tempFiles) {
      tempFiles.add(file.getAbsolutePath());
    }
    return file;
  }

  /**
   * Deletes all created files on shutdown.
   */
  @Override
  public void shutdown() {
    synchronized (tempFiles) {
      for (String path : tempFiles) {
        if (new File(path).delete()) {
          logger.trace("deleted {}", path);
        } else {
          logger.warn("could not delete {}", path);
        }
      }
      tempFiles.clear();
    }
  }

}
