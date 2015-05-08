package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.JsonConfig;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * ScalarType for ZonedDateTime.
 */
public class ScalarTypeZonedDateTime extends ScalarTypeBaseDateTime<ZonedDateTime> {

  public ScalarTypeZonedDateTime(JsonConfig.DateTime mode) {
    super(mode, ZonedDateTime.class, true, Types.TIMESTAMP);
  }

  @Override
  protected String toJsonNanos(ZonedDateTime value) {
    return toJsonNanos(value.toEpochSecond(), value.getNano());
  }

  @Override
  protected String toJsonISO8601(ZonedDateTime value) {
    return value.toInstant().toString();
  }

  @Override
  public long convertToMillis(ZonedDateTime value) {
    return value.toInstant().toEpochMilli();
  }

  @Override
  public ZonedDateTime convertFromMillis(long systemTimeMillis) {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(systemTimeMillis), ZoneId.systemDefault());
  }

  @Override
  public ZonedDateTime convertFromTimestamp(Timestamp ts) {
    return ZonedDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault());
  }

  @Override
  public Timestamp convertToTimestamp(ZonedDateTime t) {
    return Timestamp.from(t.toInstant());
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Timestamp) return value;
    return convertToTimestamp((ZonedDateTime)value);
  }

  @Override
  public ZonedDateTime toBeanType(Object value) {
    if (value instanceof ZonedDateTime) return (ZonedDateTime) value;
    return convertFromTimestamp((Timestamp)value);
  }
}
