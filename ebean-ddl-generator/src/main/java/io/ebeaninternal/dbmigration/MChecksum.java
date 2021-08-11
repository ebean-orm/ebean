package io.ebeaninternal.dbmigration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

/**
 * Calculates the checksum for the given file content.
 */
class MChecksum {

  /**
   * Returns the checksum of the file. Agnostic of encoding and new line character.
   */
  static int calculate(File file) {
    try {
      final CRC32 crc32 = new CRC32();
      BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        final byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);
        crc32.update(lineBytes, 0, lineBytes.length);
      }
      return (int) crc32.getValue();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to calculate checksum", e);
    }
  }
}
