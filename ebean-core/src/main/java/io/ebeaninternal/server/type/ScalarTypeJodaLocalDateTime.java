package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebeaninternal.server.core.BasicTypeConverter;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * ScalarType for Joda LocalDateTime. This maps to a JDBC Timestamp.
 */
final class ScalarTypeJodaLocalDateTime extends ScalarTypeBaseDateTime<LocalDateTime> {

  ScalarTypeJodaLocalDateTime(JsonConfig.DateTime mode) {
    super(mode, LocalDateTime.class, false, DbPlatformType.LOCALDATETIME, true);
  }

  @Override
  protected String toJsonNanos(LocalDateTime value) {
    return toJsonNanos(value.toDateTime(DateTimeZone.UTC).getMillis());
  }

  @Override
  protected LocalDateTime fromJsonNanos(long seconds, int nanoseconds) {
    return new LocalDateTime(seconds * 1000 + nanoseconds / 1_000_000, DateTimeZone.UTC);
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
    return value.toDateTime(DateTimeZone.UTC).getMillis();
  }

  @Override
  public LocalDateTime convertFromMillis(long systemTimeMillis) {
    return new LocalDateTime(systemTimeMillis, DateTimeZone.UTC);
  }

  @Override
  public LocalDateTime convertFromTimestamp(Timestamp ts) {
    return new LocalDateTime(ts.getTime());
  }

  @Override
  public LocalDateTime convertFromInstant(Instant ts) {
    return new LocalDateTime(ts.toEpochMilli(), DateTimeZone.UTC);
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
