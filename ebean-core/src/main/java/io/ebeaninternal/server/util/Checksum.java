package io.ebeaninternal.server.util;

import java.nio.charset.StandardCharsets;
import java.util.zip.Adler32;

/**
 * Compute a checksum for String content. Use when we desire cheaper option than MD5.
 */
public final class Checksum {

  /**
   * Return the checksum for the given String input.
   */
  public static long checksum(String input) {
    Adler32 adler32 = new Adler32();
    final byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
    adler32.update(bytes, 0, bytes.length);
    return adler32.getValue();
  }
}
