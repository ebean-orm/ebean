package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.config.JsonConfig;
import io.ebean.config.dbplatform.ExtraDbTypes;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * ScalarType for java.sql.Timestamp.
 */
final class ScalarTypeLocalDateTime extends ScalarTypeBaseDateTime<LocalDateTime> {

  ScalarTypeLocalDateTime(JsonConfig.DateTime mode) {
    super(mode, LocalDateTime.class, false, ExtraDbTypes.LOCALDATETIME, true);
  }

  @Override
  public LocalDateTime convertFromMillis(long systemTimeMillis) {
    return LocalDateTime.ofEpochSecond(systemTimeMillis / 1000, (int) (systemTimeMillis % 1000) * 1_000_000, ZoneOffset.UTC);
  }

  @Override
  public long convertToMillis(LocalDateTime value) {
    return value.toEpochSecond(ZoneOffset.UTC) * 1000 + value.getNano() / 1_000_000;
  }

  @Override
  protected String toJsonNanos(LocalDateTime value) {
    return toJsonNanos(value.toEpochSecond(ZoneOffset.UTC), value.getNano());
  }
  
  @Override
  protected LocalDateTime fromJsonNanos(long seconds, int nanoseconds) {
    return LocalDateTime.ofEpochSecond(seconds, nanoseconds, ZoneOffset.UTC);
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
    return LocalDateTime.ofInstant(ts, ZoneOffset.UTC);
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
