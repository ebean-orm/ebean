package io.ebean.core.type;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.UUID;

/**
 * Utilities to aid in the translation of decimal types to/from multiple parts.
 *
 * @author Nick Williams
 * @since 2.2.0
 */
public final class ScalarTypeUtils {

  private static final char[] ZEROES = new char[]{'0', '0', '0', '0', '0', '0', '0', '0', '0'};

  private static final BigDecimal ONE_BILLION = new BigDecimal(1000000000L);

  private static final DateTimeFormatter ISO_MILLIS = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendInstant(3)
    .toFormatter();

  /**
   * Parse using ISO8601.
   */
  public static Instant parseInstant(String jsonDateTime) {
    return Instant.parse(jsonDateTime);
  }

  /**
   * Format using ISO8601.
   */
  public static String formatInstant(Instant value) {
    return ISO_MILLIS.format(value);
  }

  /**
   * Convert decimal to duration with nanos.
   */
  public static Duration toDuration(BigDecimal value) {
    long seconds = value.longValue();
    int nanoseconds = extractNanosecondDecimal(value, seconds);
    return Duration.ofSeconds(seconds, nanoseconds);
  }

  /**
   * Convert duration to decimal with nanos.
   */
  public static BigDecimal toDecimal(Duration instant) {
    return new BigDecimal(toDecimal(instant.getSeconds(), instant.getNano()));
  }

  /**
   * Convert decimal to timestamp with nanos.
   */
  public static Timestamp toTimestamp(BigDecimal value) {
    long seconds = value.longValue();
    int nanoseconds = extractNanosecondDecimal(value, seconds);
    Timestamp ts = new Timestamp(seconds * 1000);
    ts.setNanos(nanoseconds);
    return ts;
  }

  /**
   * Convert timestamp to decimal with nanos.
   */
  public static BigDecimal toDecimal(Timestamp instant) {
    long millis = instant.getTime();
    long secs = millis / 1000;
    return new BigDecimal(toDecimal(secs, instant.getNanos()));
  }

  /**
   * Convert to decimal string with nanos.
   */
  public static String toDecimal(long seconds, int nanoseconds) {
    StringBuilder string = new StringBuilder(Integer.toString(nanoseconds));
    if (string.length() < 9)
      string.insert(0, ZEROES, 0, 9 - string.length());
    return seconds + "." + string;
  }

  private static int extractNanosecondDecimal(BigDecimal value, long integer) {
    return value.subtract(new BigDecimal(integer)).multiply(ONE_BILLION).intValue();
  }


  /**
   * Convert from byte[] to UUID.
   */
  public static UUID uuidFromBytes(byte[] bytes, boolean optimized) {
    int usableBytes = Math.min(bytes.length, 16);

    // Need exactly 16 bytes - pad the input if not enough bytes are provided
    // Use provided bytes in the least significant position; if more than 16 bytes are given,
    // then use the first 16 bytes from the array;
    byte[] barr = new byte[16];
    for (int i = 15, j = usableBytes - 1; j >= 0; i--, j--) {
      barr[i] = bytes[j];
    }

    long msb;
    if (optimized) {
      msb = ((long)barr[4] << 56) +          // XXXXXXXX-____-____-...
        ((long)(barr[5] & 255) << 48) +  // -> put at end 4..7 of buf
        ((long)(barr[6] & 255) << 40) +
        ((long)(barr[7] & 255) << 32) +
        ((long)(barr[2] & 255) << 24) +  // ________-XXXX-____-...
        ((barr[3] & 255) << 16) +        // put at 2..3 in buf
        ((barr[0] & 255) <<  8) +        // ________-____-XXXX-...
        ((barr[1] & 255) <<  0);         // put at 0..1 in buf
    } else {
      msb = ((long)barr[0] << 56) +         // XXXXXXXX-____-____-...
        ((long)(barr[1] & 255) << 48) +
        ((long)(barr[2] & 255) << 40) +
        ((long)(barr[3] & 255) << 32) +
        ((long)(barr[4] & 255) << 24) + // ________-XXXX-____-...
        ((barr[5] & 255) << 16) +
        ((barr[6] & 255) <<  8) +       // ________-____-XXXX-...
        ((barr[7] & 255) <<  0);
    }
    long lsb = ((long)barr[8] << 56) +
      ((long)(barr[9] & 255) << 48) +
      ((long)(barr[10] & 255) << 40) +
      ((long)(barr[11] & 255) << 32) +
      ((long)(barr[12] & 255) << 24) +
      ((barr[13] & 255) << 16) +
      ((barr[14] & 255) <<  8) +
      ((barr[15] & 255) <<  0);

    return new UUID(msb, lsb);
  }

  /**
   * Convert from UUID to byte[].
   */
  public static byte[] uuidToBytes(UUID uuid, boolean optimized) {
    byte[] ret = new byte[16];
    long l = uuid.getMostSignificantBits();

    if (optimized) {
      ret[0] = (byte) (l >>> 8); // was 6/7
      ret[1] = (byte) (l >>> 0);

      ret[2] = (byte) (l >>> 24); // was 4/5
      ret[3] = (byte) (l >>> 16);

      ret[4] = (byte) (l >>> 56); // was 0..3
      ret[5] = (byte) (l >>> 48);
      ret[6] = (byte) (l >>> 40);
      ret[7] = (byte) (l >>> 32);
    } else {
      ret[0] = (byte) (l >>> 56);
      ret[1] = (byte) (l >>> 48);
      ret[2] = (byte) (l >>> 40);
      ret[3] = (byte) (l >>> 32);
      ret[4] = (byte) (l >>> 24);
      ret[5] = (byte) (l >>> 16);
      ret[6] = (byte) (l >>> 8);
      ret[7] = (byte) (l >>> 0);
    }
    l = uuid.getLeastSignificantBits();
    ret[8] = (byte) (l >>> 56);
    ret[9] = (byte) (l >>> 48);
    ret[10] = (byte) (l >>> 40);
    ret[11] = (byte) (l >>> 32);
    ret[12] = (byte) (l >>> 24);
    ret[13] = (byte) (l >>> 16);
    ret[14] = (byte) (l >>> 8);
    ret[15] = (byte) (l >>> 0);

    return ret;
  }
}
