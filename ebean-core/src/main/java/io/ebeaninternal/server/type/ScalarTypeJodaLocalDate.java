package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebeaninternal.server.core.BasicTypeConverter;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Joda LocalDate. This maps to a JDBC Date.
 */
final class ScalarTypeJodaLocalDate extends ScalarTypeBaseDate<LocalDate> {

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

  @Override
  public void bind(DataBinder binder, LocalDate value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.DATE);
    } else {
      binder.setObject(java.time.LocalDate.of(value.getYear(), value.getMonthOfYear(), value.getDayOfMonth()));
    }
  }

  @Override
  public LocalDate read(DataReader reader) throws SQLException {
    java.time.LocalDate jtDate = reader.getObject(java.time.LocalDate.class);
    return jtDate == null ? null : new org.joda.time.LocalDate(jtDate.getYear(), jtDate.getMonthValue(), jtDate.getDayOfMonth());
  }
}
