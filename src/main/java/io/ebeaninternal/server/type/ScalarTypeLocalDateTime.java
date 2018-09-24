package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * ScalarType for java.sql.Timestamp.
 */
public class ScalarTypeLocalDateTime extends ScalarTypeBaseDateTime<LocalDateTime> {

  public ScalarTypeLocalDateTime(JsonConfig.DateTime mode) {
    super(mode, LocalDateTime.class, false, Types.TIMESTAMP);
  }

  @Override
  public LocalDateTime convertFromMillis(long systemTimeMillis) {
    return new Timestamp(systemTimeMillis).toLocalDateTime();
  }

  @Override
  public long convertToMillis(LocalDateTime value) {
    return Timestamp.valueOf(value).getTime();
  }

  @Override
  protected String toJsonNanos(LocalDateTime value) {
    return String.valueOf(convertToMillis(value));
  }

  @Override
  protected String toJsonISO8601(LocalDateTime dateTime) {
    return dateTime.atZone(ZoneId.systemDefault()).toInstant().toString();
  }

  @Override
  public LocalDateTime convertFromTimestamp(Timestamp timestamp) {
    return timestamp.toLocalDateTime();
  }

  @Override
  public Timestamp convertToTimestamp(LocalDateTime dateTime) {
    return Timestamp.valueOf(dateTime);
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Timestamp) return value;
    return convertToTimestamp((LocalDateTime) value);
  }

  @Override
  public LocalDateTime toBeanType(Object value) {
    if (value instanceof Timestamp) return convertFromTimestamp((Timestamp) value);
    return (LocalDateTime) value;
  }
}
