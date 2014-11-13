package com.avaje.ebeaninternal.server.type;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * ScalarType for java.sql.Timestamp.
 */
public class ScalarTypeOffsetDateTime extends ScalarTypeBaseDateTime<OffsetDateTime> {

  public ScalarTypeOffsetDateTime() {
    super(OffsetDateTime.class, true, Types.TIMESTAMP);
  }

  @Override
  public long convertToMillis(Object value) {
    return ((OffsetDateTime) value).toInstant().toEpochMilli();
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
