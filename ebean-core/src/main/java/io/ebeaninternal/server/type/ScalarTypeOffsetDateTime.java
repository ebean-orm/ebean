package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static io.ebeaninternal.server.type.IsoJsonDateTimeParser.formatIso;

/**
 * ScalarType for java.sql.Timestamp.
 */
public class ScalarTypeOffsetDateTime extends ScalarTypeBaseDateTime<OffsetDateTime> {

  public ScalarTypeOffsetDateTime(JsonConfig.DateTime mode) {
    super(mode, OffsetDateTime.class, false, Types.TIMESTAMP);
  }

  @Override
  protected String toJsonNanos(OffsetDateTime value) {
    return toJsonNanos(value.toEpochSecond(), value.getNano());
  }

  @Override
  protected String toJsonISO8601(OffsetDateTime value) {
    return formatIso(value.toInstant());
  }

  @Override
  public long convertToMillis(OffsetDateTime value) {
    return value.toInstant().toEpochMilli();
  }

  @Override
  public OffsetDateTime convertFromMillis(long systemTimeMillis) {
    return convertFromInstant(Instant.ofEpochMilli(systemTimeMillis));
  }

  @Override
  public OffsetDateTime convertFromTimestamp(Timestamp ts) {
    return convertFromInstant(ts.toInstant());
  }

  @Override
  public OffsetDateTime convertFromInstant(Instant ts) {
    return OffsetDateTime.ofInstant(ts, ZoneId.systemDefault());
  }

  @Override
  public Timestamp convertToTimestamp(OffsetDateTime t) {
    return Timestamp.from(t.toInstant());
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Timestamp) return value;
    return convertToTimestamp((OffsetDateTime) value);
  }

  @Override
  public OffsetDateTime toBeanType(Object value) {
    if (value instanceof Timestamp) return convertFromTimestamp((Timestamp) value);
    return (OffsetDateTime) value;
  }
}
