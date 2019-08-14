package io.ebean.util;

/**
 * Integer to base64 encoder.
 */
public final class EncodeB64 {

  private static final int radix = 1 << 6;
  private static final int mask = radix - 1;

  /**
   * Convert the integer to unsigned base 64.
   */
  public static String enc(int i) {
    char[] buf = new char[32];
    int charPos = 32;
    do {
      buf[--charPos] = intToBase64[i & mask];
      i >>>= 6;
    } while (i != 0);

    return new String(buf, charPos, (32 - charPos));
  }

  private static final char intToBase64[] = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
  };
}
