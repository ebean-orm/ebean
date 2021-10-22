package io.ebean.config;

import java.io.File;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WeakRefTempFileProvider will delete the tempFile if all references to the returned File
 * object are collected by the garbage collection.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class WeakRefTempFileProvider implements TempFileProvider {

  private static final Logger logger = LoggerFactory.getLogger(WeakRefTempFileProvider.class);

  private final ReferenceQueue<File> tempFiles = new ReferenceQueue<>();

  private WeakFileReference root;

  private final String prefix;
  private final String suffix;
  private final File directory;

  /**
   * We hold a linkedList of weak references. So we can remove stale files in O(1)
   *
   * @author Roland Praml, FOCONIS AG
   */
  private static class WeakFileReference extends WeakReference<File> {

    String path;
    WeakFileReference prev;
    WeakFileReference next;

    WeakFileReference(File referent, ReferenceQueue<? super File> q) {
      super(referent, q);
      path = referent.getAbsolutePath();
    }

    boolean delete(boolean shutdown) {
      if (new File(path).delete()) {
        logger.trace("deleted {}", path);
        return true;
      } else {
        if (shutdown) {
          logger.warn("could not delete {}", path);
        } else {
          logger.info("could not delete {} - will delete on shutdown", path);
        }
        return false;
      }
    }
  }


  /**
   * Creates the TempFileProvider with default prefix "db-".
   */
  public WeakRefTempFileProvider() {
    this("db-", null, null);
  }

  /**
   * Creates the TempFileProvider.
   */
  public WeakRefTempFileProvider(String prefix, String suffix, File directory) {
    this.prefix = prefix;
    this.suffix = suffix;
    this.directory = directory;
  }

  @Override
  public File createTempFile() throws IOException {
    File tempFile = File.createTempFile(prefix, suffix, directory);
    logger.trace("createTempFile: {}", tempFile);
    synchronized (this) {
      add(new WeakFileReference(tempFile, tempFiles));
    }
    return tempFile;
  }

  /**
   * Will delete stale files.
   * This is public to use in tests.
   */
  public void deleteStaleTempFiles() {
    synchronized (this) {
      deleteStaleTempFilesInternal();
    }
  }

  private void deleteStaleTempFilesInternal() {
    WeakFileReference ref;
    while ((ref = (WeakFileReference) tempFiles.poll()) != null) {
      if (ref.delete(false)) {
        remove(ref); // remove from linkedList only, if delete was successful.
      }
    }
  }

  private void add(WeakFileReference ref) {
    deleteStaleTempFilesInternal();

    if (root == null) {
      root = ref;
    } else {
      ref.next = root;
      root.prev = ref;
      root = ref;
    }
  }

  private void remove(WeakFileReference ref) {
    if (ref.next != null) {
      ref.next.prev = ref.prev;
    }
    if (ref.prev != null) {
      ref.prev.next = ref.next;
    } else {
      root = ref.next;
    }
  }

  /**
   * Deletes all created files on shutdown.
   */
  @Override
  public void shutdown() {
    while (root != null) {
      root.delete(true);
      root = root.next;
    }
  }

}
