package com.avaje.ebeaninternal.server.type;

import java.sql.Timestamp;
import java.sql.Types;

import com.avaje.ebean.config.JsonConfig;
import org.joda.time.DateTime;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import org.joda.time.LocalDateTime;

/**
 * ScalarType for Joda DateTime. This maps to a JDBC Timestamp.
 */
public class ScalarTypeJodaDateTime extends ScalarTypeBaseDateTime<DateTime> {

  public ScalarTypeJodaDateTime(JsonConfig.DateTime mode) {
    super(mode, DateTime.class, false, Types.TIMESTAMP);
  }

  @Override
  public long convertToMillis(DateTime value) {
    return value.getMillis();
  }

  @Override
  protected String toJsonNanos(DateTime value) {
    return String.valueOf(value.toDateTime().getMillis());
  }

  @Override
  protected String toJsonISO8601(DateTime value) {
    return value.toString();
  }

  @Override
  public DateTime convertFromMillis(long systemTimeMillis) {
    return new DateTime(systemTimeMillis);
  }

  @Override
  public DateTime convertFromTimestamp(Timestamp ts) {
    return new DateTime(ts.getTime());
  }

  @Override
  public Timestamp convertToTimestamp(DateTime t) {
    return new Timestamp(t.getMillis());
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof DateTime) {
      return new Timestamp(((DateTime) value).getMillis());
    }
    return BasicTypeConverter.toTimestamp(value);
  }

  @Override
  public DateTime toBeanType(Object value) {
    if (value instanceof java.util.Date) {
      return new DateTime(((java.util.Date) value).getTime());
    }
    return (DateTime) value;
  }

}
