package com.avaje.ebeaninternal.server.type;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

import com.avaje.ebean.config.JsonConfig;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for java.util.Calendar.
 */
public class ScalarTypeCalendar extends ScalarTypeBaseDateTime<Calendar> {

  public ScalarTypeCalendar(JsonConfig.DateTime mode, int jdbcType) {
    super(mode, Calendar.class, false, jdbcType);
  }

  public void bind(DataBind b, Calendar value) throws SQLException {
    if (value == null) {
      b.setNull(Types.TIMESTAMP);
    } else {

      if (jdbcType == Types.TIMESTAMP) {
        Timestamp timestamp = new Timestamp(value.getTimeInMillis());
        b.setTimestamp(timestamp);
      } else {
        Date d = new Date(value.getTimeInMillis());
        b.setDate(d);
      }
    }
  }

  @Override
  public Calendar convertFromMillis(long systemTimeMillis) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(systemTimeMillis);
    return calendar;
  }

  @Override
  public Calendar convertFromTimestamp(Timestamp ts) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(ts.getTime());
    return calendar;
  }

  @Override
  protected String toJsonNanos(Calendar value) {
    return String.valueOf(value.getTime());
  }

  @Override
  protected String toJsonISO8601(Calendar value) {
    return dateTimeParser.format(value.getTime());
  }

  @Override
  public long convertToMillis(Calendar value) {
    return value.getTimeInMillis();
  }

  @Override
  public Timestamp convertToTimestamp(Calendar t) {
    return new Timestamp(t.getTimeInMillis());
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.convert(value, jdbcType);
  }

  @Override
  public Calendar toBeanType(Object value) {
    return BasicTypeConverter.toCalendar(value);
  }

}
