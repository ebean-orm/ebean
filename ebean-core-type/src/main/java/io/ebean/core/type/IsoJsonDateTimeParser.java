package io.ebean.core.type;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public final class IsoJsonDateTimeParser {

  private static final DateTimeFormatter ISO_MILLIS = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendInstant(3)
    .toFormatter();

  public static Instant parseIso(String jsonDateTime) {
    return Instant.parse(jsonDateTime);
  }

  public static String formatIso(Instant value) {
    return ISO_MILLIS.format(value);
  }
}
