package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalTime;

/**
 * ScalarType for java.time.LocalTime stored with Nanos as DB BIGINT type.
 */
final class ScalarTypeLocalTimeWithNanos extends ScalarTypeLocalTime {

  ScalarTypeLocalTimeWithNanos() {
    super(Types.BIGINT);
  }

  @Override
  public void bind(DataBinder binder, LocalTime value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.BIGINT);
    } else {
      binder.setLong(value.toNanoOfDay());
    }
  }

  @Override
  public LocalTime read(DataReader reader) throws SQLException {
    Long value = reader.getLong();
    return (value == null) ? null : LocalTime.ofNanoOfDay(value);
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Long) return value;
    return ((LocalTime) value).toNanoOfDay();
  }

  @Override
  public LocalTime toBeanType(Object value) {
    if (value instanceof LocalTime) return (LocalTime) value;
    if (value == null) return null;
    return LocalTime.ofNanoOfDay(BasicTypeConverter.toLong(value));
  }

}
