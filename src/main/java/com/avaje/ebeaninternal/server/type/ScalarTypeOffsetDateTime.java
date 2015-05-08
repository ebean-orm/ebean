package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.JsonConfig;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * ScalarType for java.sql.Timestamp.
 */
public class ScalarTypeOffsetDateTime extends ScalarTypeBaseDateTime<OffsetDateTime> {

  public ScalarTypeOffsetDateTime(JsonConfig.DateTime mode) {
    super(mode, OffsetDateTime.class, true, Types.TIMESTAMP);
  }

  @Override
  protected String toJsonNanos(OffsetDateTime value) {
    return toJsonNanos(value.toEpochSecond(), value.getNano());
  }

  @Override
  protected String toJsonISO8601(OffsetDateTime value) {
    return value.toInstant().toString();
  }

  @Override
  public long convertToMillis(OffsetDateTime value) {
    return value.toInstant().toEpochMilli();
  }

  @Override
  public OffsetDateTime convertFromMillis(long systemTimeMillis) {
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(systemTimeMillis), ZoneId.systemDefault());
  }

  @Override
  public OffsetDateTime convertFromTimestamp(Timestamp ts) {
    return OffsetDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault());
  }

  @Override
  public Timestamp convertToTimestamp(OffsetDateTime t) {
    return Timestamp.from(t.toInstant());
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Timestamp) return value;
    return convertToTimestamp((OffsetDateTime)value);
  }

  @Override
  public OffsetDateTime toBeanType(Object value) {
    if (value instanceof OffsetDateTime) return (OffsetDateTime) value;
    return convertFromTimestamp((Timestamp)value);
  }
}
