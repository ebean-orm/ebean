package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.config.JsonConfig;

import java.io.IOException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * ScalarType for java.sql.Timestamp.
 */
final class ScalarTypeLocalDateTime extends ScalarTypeBaseDateTime<LocalDateTime> {

  ScalarTypeLocalDateTime(JsonConfig.DateTime mode) {
    super(mode, LocalDateTime.class, false, Types.TIMESTAMP);
  }

  @Override
  public LocalDateTime convertFromMillis(long systemTimeMillis) {
    return new Timestamp(systemTimeMillis).toLocalDateTime();
  }

  @Override
  public long convertToMillis(LocalDateTime value) {
    return Timestamp.valueOf(value).getTime();
  }

  @Override
  protected String toJsonNanos(LocalDateTime value) {
    return value.toString();
  }

  @Override
  protected String toJsonISO8601(LocalDateTime value) {
    return value.toString();
  }

  @Override
  protected LocalDateTime fromJsonISO8601(String value) {
    return LocalDateTime.parse(value);
  }

  @Override
  public LocalDateTime jsonRead(JsonParser parser) throws IOException {
    return LocalDateTime.parse(parser.getText());
  }

  @Override
  public void jsonWrite(JsonGenerator writer, LocalDateTime value) throws IOException {
    writer.writeString(value.toString());
  }

  @Override
  public String formatValue(LocalDateTime value) {
    return value.toString();
  }

  @Override
  public LocalDateTime parse(String value) {
    return LocalDateTime.parse(value);
  }

  @Override
  public LocalDateTime convertFromTimestamp(Timestamp timestamp) {
    return timestamp.toLocalDateTime();
  }

  @Override
  public LocalDateTime convertFromInstant(Instant ts) {
    return LocalDateTime.ofInstant(ts, ZoneId.systemDefault());
  }

  @Override
  public Timestamp convertToTimestamp(LocalDateTime dateTime) {
    return Timestamp.valueOf(dateTime);
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Timestamp) return value;
    return convertToTimestamp((LocalDateTime) value);
  }

  @Override
  public LocalDateTime toBeanType(Object value) {
    if (value instanceof Timestamp) return convertFromTimestamp((Timestamp) value);
    return (LocalDateTime) value;
  }
}
