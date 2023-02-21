package io.ebean.joda.time;

import io.ebean.config.JsonConfig;
import io.ebean.core.type.ScalarTypeBaseDateTime;
import io.ebean.core.type.BasicTypeConverter;
import org.joda.time.LocalDateTime;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

/**
 * ScalarType for Joda LocalDateTime. This maps to a JDBC Timestamp.
 */
final class ScalarTypeJodaLocalDateTime extends ScalarTypeBaseDateTime<LocalDateTime> {

  ScalarTypeJodaLocalDateTime(JsonConfig.DateTime mode) {
    super(mode, LocalDateTime.class, false, Types.TIMESTAMP);
  }

  @Override
  protected String toJsonNanos(LocalDateTime value) {
    return String.valueOf(value.toDateTime().getMillis());
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
  public long convertToMillis(LocalDateTime value) {
    return value.toDateTime().getMillis();
  }

  @Override
  public LocalDateTime convertFromMillis(long systemTimeMillis) {
    return new LocalDateTime(systemTimeMillis);
  }

  @Override
  public LocalDateTime convertFromTimestamp(Timestamp ts) {
    return new LocalDateTime(ts.getTime());
  }

  @Override
  public LocalDateTime convertFromInstant(Instant ts) {
    return new LocalDateTime(ts.toEpochMilli());
  }

  @Override
  public Timestamp convertToTimestamp(LocalDateTime t) {
    return new Timestamp(t.toDateTime().getMillis());
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof LocalDateTime) {
      return new Timestamp(((LocalDateTime) value).toDateTime().getMillis());
    }
    return BasicTypeConverter.toTimestamp(value);
  }

  @Override
  public LocalDateTime toBeanType(Object value) {
    if (value instanceof java.util.Date) {
      return new LocalDateTime(((java.util.Date) value).getTime());
    }
    return (LocalDateTime) value;
  }

}
