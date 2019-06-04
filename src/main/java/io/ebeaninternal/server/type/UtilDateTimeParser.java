package io.ebeaninternal.server.type;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Date;

class UtilDateTimeParser {

  private static final DateTimeFormatter ISO_MILLIS = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendInstant(3)
    .toFormatter();

  public Timestamp parse(String jsonDateTime) {
    try {
      return Timestamp.from(Instant.parse(jsonDateTime));
    } catch (DateTimeParseException e) {
      throw new RuntimeException("Error parsing Datetime[" + jsonDateTime + "]", e);
    }
  }

  public String format(Date value) {
    return ISO_MILLIS.format(value.toInstant());
  }
}
