package io.ebeaninternal.server.type;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

class IsoJsonDateTimeParser {

  private static final DateTimeFormatter ISO_MILLIS = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendInstant(3)
    .toFormatter();

  static Instant parseIso(String jsonDateTime) {
    return Instant.parse(jsonDateTime);
  }

  static String formatIso(Instant value) {
    return ISO_MILLIS.format(value);
  }
}
