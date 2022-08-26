package io.ebean.joda.time;

import java.sql.Date;
import java.sql.Types;

import io.ebean.core.type.ScalarTypeBaseDate;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import io.ebean.config.JsonConfig;
import io.ebean.core.type.BasicTypeConverter;

/**
 * ScalarType for Joda LocalDate. This maps to a JDBC Date.
 */
class ScalarTypeJodaLocalDate extends ScalarTypeBaseDate<LocalDate> {

  ScalarTypeJodaLocalDate(JsonConfig.Date mode) {
    super(mode, LocalDate.class, false, Types.DATE);
  }

  @Override
  protected String toIsoFormat(LocalDate value) {
    return value.toString();
  }

  @Override
  public LocalDate convertFromMillis(long systemTimeMillis) {
    return new LocalDate(systemTimeMillis);
  }

  @Override
  public long convertToMillis(LocalDate value) {
    return value.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis();
  }

  @Override
  public LocalDate convertFromDate(Date date) {
    return LocalDate.fromDateFields(date);
  }

  @SuppressWarnings("deprecation")
  @Override
  public Date convertToDate(LocalDate value) {
    return new Date(value.getYear() - 1900, value.getMonthOfYear() - 1, value.getDayOfMonth());
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
      return LocalDate.fromDateFields((java.util.Date) value);
    }
    return (LocalDate) value;
  }
}
