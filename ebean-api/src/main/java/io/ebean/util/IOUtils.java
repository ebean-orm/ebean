package io.ebean.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Utilities for IO. It uses UTF-8 as encoding when reading/writing and uses
 * buffered IO for better performance.
 */
public class IOUtils {

  /**
   * Read from stream as UTF-8.
   */
  public static BufferedReader newReader(InputStream is) {
    return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
  }

  /**
   * Read from file as UTF-8.
   */
  public static BufferedReader newReader(File file) throws FileNotFoundException {
    return newReader(new FileInputStream(file));
  }

  /**
   * Write to stream as UTF-8
   */
  public static BufferedWriter newWriter(OutputStream os) {
    return new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
  }

  /**
   * Write to file as UTF-8
   */
  public static BufferedWriter newWriter(File file) throws FileNotFoundException {
    return newWriter(new FileOutputStream(file));
  }
}
