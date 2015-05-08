package com.avaje.ebeaninternal.server.type;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalTime;

/**
 * ScalarType for java.time.LocalTime stored with Nanos as DB BIGINT type.
 */
public class ScalarTypeLocalTimeWithNanos extends ScalarTypeLocalTime {

  public ScalarTypeLocalTimeWithNanos() {
    super(Types.BIGINT);
  }

  @Override
  public void bind(DataBind bind, LocalTime value) throws SQLException {
    if (value == null) {
      bind.setNull(Types.BIGINT);
    } else {
      bind.setLong(value.toNanoOfDay());
    }
  }

  @Override
  public LocalTime read(DataReader dataReader) throws SQLException {
    Long value = dataReader.getLong();
    return (value == null) ? null : LocalTime.ofNanoOfDay(value);
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof  Long) return value;
    return ((LocalTime)value).toNanoOfDay();
  }

  @Override
  public LocalTime toBeanType(Object value) {
    if (value instanceof LocalTime) return (LocalTime) value;
    return LocalTime.ofNanoOfDay(BasicTypeConverter.toLong(value));
  }

}
