package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.JsonConfig;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

/**
 * ScalarType for java.util.Date.
 */
public class ScalarTypeUtilDate {

  public static class TimestampType extends ScalarTypeBaseDateTime<Date> {

    public TimestampType(JsonConfig.DateTime mode) {
      super(mode, Date.class, false, Types.TIMESTAMP);
    }

    @Override
    protected String toJsonNanos(Date value) {
      return String.valueOf(value.getTime());
    }

    @Override
    protected String toJsonISO8601(Date value) {
      return dateTimeParser.format(value);
    }

    @Override
    public long convertToMillis(Date value) {
      return value.getTime();
    }

    @Override
    public Date read(DataReader dataReader) throws SQLException {
      Timestamp timestamp = dataReader.getTimestamp();
      if (timestamp == null) {
        return null;
      } else {
        return new Date(timestamp.getTime());
      }
    }

    @Override
    public void bind(DataBind dataBind, Date value) throws SQLException {
      if (value == null) {
        dataBind.setNull(Types.TIMESTAMP);
      } else {
        dataBind.setTimestamp(new Timestamp(value.getTime()));
      }
    }

    @Override
    public Object toJdbcType(Object value) {
      return BasicTypeConverter.toTimestamp(value);
    }

    @Override
    public Date toBeanType(Object value) {
      return BasicTypeConverter.toUtilDate(value);
    }


    @Override
    public Date convertFromTimestamp(Timestamp ts) {
      return new Date(ts.getTime());
    }

    @Override
    public Timestamp convertToTimestamp(Date date) {
      return new Timestamp(date.getTime());
    }

    @Override
    public Date convertFromMillis(long systemTimeMillis) {
      return new Date(systemTimeMillis);
    }
  }

  public static class DateType extends ScalarTypeBaseDate<Date> {

    public DateType() {
      super(Date.class, false, Types.DATE);
    }


    @Override
    public long convertToMillis(Date value) {
      return value.getTime();
    }

    @Override
    public Date convertFromDate(java.sql.Date ts) {
      return new Date(ts.getTime());
    }

    @Override
    public java.sql.Date convertToDate(Date date) {
      return new java.sql.Date(date.getTime());
    }

    @Override
    public Object toJdbcType(Object value) {
      return BasicTypeConverter.toDate(value);
    }

    @Override
    public Date toBeanType(Object value) {
      return BasicTypeConverter.toUtilDate(value);
    }
  }
}
