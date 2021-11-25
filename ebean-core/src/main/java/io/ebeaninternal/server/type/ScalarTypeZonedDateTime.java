package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * ScalarType for ZonedDateTime.
 */
final class ScalarTypeZonedDateTime extends ScalarTypeBaseDateTime<ZonedDateTime> {

  private final ZoneId zoneId;

  ScalarTypeZonedDateTime(JsonConfig.DateTime mode, ZoneId zoneId) {
    super(mode, ZonedDateTime.class, false, Types.TIMESTAMP, false);
    this.zoneId = zoneId;
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
  protected ZonedDateTime fromJsonISO8601(String value) {
    return convertFromInstant(Instant.parse(value));
  }

  @Override
  public long convertToMillis(ZonedDateTime value) {
    return value.toInstant().toEpochMilli();
  }

  @Override
  public ZonedDateTime convertFromMillis(long systemTimeMillis) {
    return convertFromInstant(Instant.ofEpochMilli(systemTimeMillis));
  }

  @Override
  public ZonedDateTime convertFromTimestamp(Timestamp ts) {
    return convertFromInstant(ts.toInstant());
  }

  @Override
  public ZonedDateTime convertFromInstant(Instant ts) {
    return ZonedDateTime.ofInstant(ts, zoneId);
  }

  @Override
  public Timestamp convertToTimestamp(ZonedDateTime t) {
    return Timestamp.from(t.toInstant());
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Timestamp) return value;
    return convertToTimestamp((ZonedDateTime) value);
  }

  @Override
  public ZonedDateTime toBeanType(Object value) {
    if (value instanceof Timestamp) return convertFromTimestamp((Timestamp) value);
    return (ZonedDateTime) value;
  }
}
