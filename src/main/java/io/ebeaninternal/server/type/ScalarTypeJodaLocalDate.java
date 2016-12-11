package io.ebeaninternal.server.type;

import io.ebeaninternal.server.core.BasicTypeConverter;
import org.joda.time.LocalDate;

import java.sql.Date;
import java.sql.Types;

/**
 * ScalarType for Joda LocalDate. This maps to a JDBC Date.
 */
public class ScalarTypeJodaLocalDate extends ScalarTypeBaseDate<LocalDate> {

  public ScalarTypeJodaLocalDate() {
    super(LocalDate.class, false, Types.DATE);
  }

  @Override
  public LocalDate convertFromMillis(long systemTimeMillis) {
    return new LocalDate(systemTimeMillis);
  }

  @Override
  public long convertToMillis(LocalDate value) {
    return value.toDateTimeAtStartOfDay().getMillis();
  }

  @Override
  public LocalDate convertFromDate(Date date) {
    return new LocalDate(date.getTime());
  }

  @Override
  public Date convertToDate(LocalDate value) {
    return new java.sql.Date(convertToMillis(value));
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof LocalDate) {
      return convertToDate((LocalDate) value);
    }
    return BasicTypeConverter.toDate(value);
  }

  @Override
  public LocalDate toBeanType(Object value) {
    if (value instanceof java.util.Date) {
      return convertFromMillis(((java.util.Date) value).getTime());
    }
    return (LocalDate) value;
  }

}
