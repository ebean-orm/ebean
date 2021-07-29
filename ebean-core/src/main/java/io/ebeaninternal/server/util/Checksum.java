package io.ebeaninternal.server.util;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

/**
 * Compute a checksum for String content. Use when we desire cheaper option than MD5.
 */
public final class Checksum {

  /**
   * Return the checksum for the given String input.
   */
  public static long checksum(String input) {
    CRC32 checksum = new CRC32();
    final byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
    checksum.update(bytes, 0, bytes.length);
    return checksum.getValue();
  }
}
