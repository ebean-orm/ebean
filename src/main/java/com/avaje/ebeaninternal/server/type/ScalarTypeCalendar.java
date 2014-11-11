package com.avaje.ebeaninternal.server.type;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for java.util.Calendar.
 */
public class ScalarTypeCalendar extends ScalarTypeBaseDateTime<Calendar> {

  public ScalarTypeCalendar(int jdbcType) {
    super(Calendar.class, false, jdbcType);
  }

  public void bind(DataBind b, Calendar value) throws SQLException {
    if (value == null) {
      b.setNull(Types.TIMESTAMP);
    } else {
      Calendar date = (Calendar) value;
      if (jdbcType == Types.TIMESTAMP) {
        Timestamp timestamp = new Timestamp(date.getTimeInMillis());
        b.setTimestamp(timestamp);
      } else {
        Date d = new Date(date.getTimeInMillis());
        b.setDate(d);
      }
    }
  }

  @Override
  public Calendar convertFromTimestamp(Timestamp ts) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(ts.getTime());
    return calendar;
  }

  @Override
  public long convertToMillis(Object value) {
    return ((Calendar) value).getTimeInMillis();
  }

  @Override
  public Timestamp convertToTimestamp(Calendar t) {
    return new Timestamp(t.getTimeInMillis());
  }

  public Object toJdbcType(Object value) {
    return BasicTypeConverter.convert(value, jdbcType);
  }

  public Calendar toBeanType(Object value) {
    return BasicTypeConverter.toCalendar(value);
  }

}
