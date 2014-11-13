package com.avaje.ebeaninternal.server.type;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.*;

/**
 * ScalarType for java.sql.Timestamp.
 */
public class ScalarTypeLocalDateTime extends ScalarTypeBaseDateTime<LocalDateTime> {

  public ScalarTypeLocalDateTime() {
    super(LocalDateTime.class, true, Types.TIMESTAMP);
  }

  @Override
  public long convertToMillis(Object value) {
    return ((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
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
