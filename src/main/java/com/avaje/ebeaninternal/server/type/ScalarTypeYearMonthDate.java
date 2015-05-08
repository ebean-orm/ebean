package com.avaje.ebeaninternal.server.type;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * ScalarType for java.time.LocalDate. This maps to a JDBC Date.
 */
public class ScalarTypeYearMonthDate extends ScalarTypeBaseDate<YearMonth> {

  public ScalarTypeYearMonthDate() {
    super(YearMonth.class, false, Types.DATE);
  }

  protected LocalDate toLocalDate(YearMonth yearMonth) {
    return LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1);
  }

  protected YearMonth fromLocalDate(LocalDate localDate) {
    return YearMonth.of(localDate.getYear(), localDate.getMonth());
  }

  @Override
  public YearMonth convertFromMillis(long systemTimeMillis) {
    return fromLocalDate(new Timestamp(systemTimeMillis).toLocalDateTime().toLocalDate());
  }

  @Override
  public long convertToMillis(YearMonth value) {
    LocalDate localDate = toLocalDate(value);
    ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
    return zonedDateTime.toInstant().toEpochMilli();
  }

  @Override
  public YearMonth convertFromDate(Date ts) {
    return fromLocalDate(ts.toLocalDate());
  }

  @Override
  public Date convertToDate(YearMonth t) {
    return Date.valueOf(toLocalDate(t));
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Date) return value;
    if (value instanceof YearMonth) return Date.valueOf(toLocalDate((YearMonth) value));
    if (value instanceof LocalDate) return Date.valueOf((LocalDate)value);
    return BasicTypeConverter.toDate(value);
  }

  @Override
  public YearMonth toBeanType(Object value) {
    if (value instanceof YearMonth) return (YearMonth) value;
    if (value instanceof LocalDate) return fromLocalDate((LocalDate) value);
    return fromLocalDate(BasicTypeConverter.toDate(value).toLocalDate());
  }

}
