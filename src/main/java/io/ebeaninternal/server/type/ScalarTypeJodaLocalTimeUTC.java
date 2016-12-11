package io.ebeaninternal.server.type;

import io.ebeaninternal.server.core.BasicTypeConverter;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;

/**
 * ScalarType for Joda LocalTime. This maps to a JDBC Time.
 */
public class ScalarTypeJodaLocalTimeUTC extends ScalarTypeJodaLocalTime {

  public ScalarTypeJodaLocalTimeUTC() {
    super();
  }

  @Override
  public void bind(DataBind b, LocalTime value) throws SQLException {
    if (value == null) {
      b.setNull(Types.TIME);
    } else {
      Time sqlTime = new Time(value.getMillisOfDay());
      b.setTime(sqlTime);
    }
  }

  @Override
  public LocalTime read(DataReader dataReader) throws SQLException {

    Time sqlTime = dataReader.getTime();
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
