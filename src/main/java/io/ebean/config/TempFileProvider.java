package io.ebean.config;

import java.io.File;
import java.io.IOException;

/**
 * Creates a temp file for the ScalarTypeFile datatype.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public interface TempFileProvider {

  /**
   * Creates a tempFile.
   */
  File createTempFile() throws IOException;

  /**
   * Shutdown the tempFileProvider.
   */
  void shutdown();
}
