package com.avaje.ebean.dbmigration.runner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.zip.CRC32;

/**
 * Calculates the checksum for the given string content.
 */
public class Checksum {

  /**
   * Returns the checksum of this string.
   */
  public static int calculate(String str) {

    final CRC32 crc32 = new CRC32();

    BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
    try {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        crc32.update(line.getBytes("UTF-8"));
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to calculate checksum", e);
    }

    return (int) crc32.getValue();
  }
}
