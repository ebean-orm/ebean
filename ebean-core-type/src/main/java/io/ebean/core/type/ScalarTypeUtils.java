package io.ebean.core.type;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

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
}
