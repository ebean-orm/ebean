package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.JsonConfig;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

/**
 * ScalarType for java.sql.Timestamp.
 */
public class ScalarTypeInstant extends ScalarTypeBaseDateTime<Instant> {

  public ScalarTypeInstant(JsonConfig.DateTime mode) {
    super(mode, Instant.class, true, Types.TIMESTAMP);
  }

  @Override
  protected String toJsonNanos(Instant value) {
    return toJsonNanos(value.getEpochSecond(), value.getNano());
  }

  @Override
  protected String toJsonISO8601(Instant value) {
    return value.toString();
  }

  @Override
  public long convertToMillis(Instant value) {
    return value.toEpochMilli();
  }

  @Override
  public Instant convertFromMillis(long systemTimeMillis) {
    return Instant.ofEpochMilli(systemTimeMillis);
  }

  @Override
  public Instant convertFromTimestamp(Timestamp ts) {
    return ts.toInstant();
  }

  @Override
  public Timestamp convertToTimestamp(Instant t) {
    return Timestamp.from(t);
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof  Timestamp) return value;
    return convertToTimestamp((Instant) value);
  }

  @Override
  public Instant toBeanType(Object value) {
    if (value instanceof  Instant) return (Instant) value;
    return convertFromTimestamp((Timestamp) value);
  }
}
