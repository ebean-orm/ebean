package io.ebean.joda.time;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.BasicTypeConverter;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;

/**
 * ScalarType for Joda LocalTime. This maps to a JDBC Time.
 */
final class ScalarTypeJodaLocalTimeUTC extends ScalarTypeJodaLocalTime {

  ScalarTypeJodaLocalTimeUTC() {
    super();
  }

  @Override
  public void bind(DataBinder binder, LocalTime value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.TIME);
    } else {
      Time sqlTime = new Time(value.getMillisOfDay());
      binder.setTime(sqlTime);
    }
  }

  @Override
  public LocalTime read(DataReader reader) throws SQLException {
    Time sqlTime = reader.getTime();
    if (sqlTime == null) {
      return null;
    } else {
      return new LocalTime(sqlTime, DateTimeZone.UTC);
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof LocalTime) {
      return new Time(((LocalTime) value).getMillisOfDay());
    }
    return BasicTypeConverter.toTime(value);
  }

  @Override
  public LocalTime toBeanType(Object value) {
    if (value instanceof java.util.Date) {
      return new LocalTime(value, DateTimeZone.UTC);
    }
    return (LocalTime) value;
  }

  @Override
  public LocalTime convertFromMillis(long systemTimeMillis) {
    return new LocalTime(systemTimeMillis);
  }

}
