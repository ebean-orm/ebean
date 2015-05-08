package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.JsonConfig;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * ScalarType for java.sql.Timestamp.
 */
public class ScalarTypeLocalDateTime extends ScalarTypeBaseDateTime<LocalDateTime> {

  public ScalarTypeLocalDateTime(JsonConfig.DateTime mode) {
    super(mode, LocalDateTime.class, true, Types.TIMESTAMP);
  }

  @Override
  public LocalDateTime convertFromMillis(long systemTimeMillis) {
    return new Timestamp(systemTimeMillis).toLocalDateTime();
  }

  @Override
  public long convertToMillis(LocalDateTime value) {
    return value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }

  @Override
  protected String toJsonNanos(LocalDateTime value) {
    return String.valueOf(convertToMillis(value));
  }

  @Override
  protected String toJsonISO8601(LocalDateTime value) {
    return value.atZone(ZoneId.systemDefault()).toInstant().toString();
  }

  @Override
  public LocalDateTime convertFromTimestamp(Timestamp ts) {
    return ts.toLocalDateTime();
  }

  @Override
  public Timestamp convertToTimestamp(LocalDateTime t) {

    ZonedDateTime zonedDateTime = t.atZone(ZoneId.systemDefault());
    return Timestamp.from(zonedDateTime.toInstant());
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Timestamp) return value;
    return convertToTimestamp((LocalDateTime)value);
  }

  @Override
  public LocalDateTime toBeanType(Object value) {
    if (value instanceof LocalDateTime) return (LocalDateTime)value;
    return convertFromTimestamp((Timestamp)value);
  }
}
