package io.ebeaninternal.server.type;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

final class IsoJsonDateTimeParser {

  private static final DateTimeFormatter ISO_MILLIS = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendInstant(3)
    .toFormatter();

  static Instant parseIso(String jsonDateTime) {
    return Instant.parse(jsonDateTime);
  }

  /**
   * Formats the instant with milliseconds precision.
   */
  static String formatIso(Instant value) {
    return ISO_MILLIS.format(value);
  }
}
