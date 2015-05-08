package com.avaje.ebeaninternal.server.type;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * ScalarType for java.time.LocalDate. This maps to a JDBC Date.
 */
public class ScalarTypeLocalDate extends ScalarTypeBaseDate<LocalDate> {

  public ScalarTypeLocalDate() {
    super(LocalDate.class, false, Types.DATE);
  }

  @Override
  public long convertToMillis(LocalDate value) {
    ZonedDateTime zonedDateTime = value.atStartOfDay(ZoneId.systemDefault());
    return zonedDateTime.toInstant().toEpochMilli();
  }

  @Override
  public LocalDate convertFromDate(Date ts) {
    return ts.toLocalDate();
  }

  @Override
  public Date convertToDate(LocalDate t) {
    return Date.valueOf(t);
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof LocalDate) {
      return Date.valueOf((LocalDate) value);
    }
    return BasicTypeConverter.toDate(value);
  }

  @Override
  public LocalDate toBeanType(Object value) {
    if (value instanceof java.sql.Date) {
      return ((java.sql.Date) value).toLocalDate();
    }
    return (LocalDate) value;
  }

  @Override
  public LocalDate convertFromMillis(long systemTimeMillis) {
    return new Timestamp(systemTimeMillis).toLocalDateTime().toLocalDate();
  }


}
