package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * ScalarType for java.time.LocalDate. This maps to a JDBC Date.
 */
final class ScalarTypeLocalDate extends ScalarTypeBaseDate<LocalDate> {

  ScalarTypeLocalDate(JsonConfig.Date mode) {
    super(mode, LocalDate.class, false, Types.DATE);
  }

  @Override
  protected String toIsoFormat(LocalDate value) {
    return value.toString();
  }

  @Override
  public LocalDate convertFromMillis(long systemTimeMillis) {
    return new Timestamp(systemTimeMillis).toLocalDateTime().toLocalDate();
  }

  @Override
  public long convertToMillis(LocalDate value) {
    ZonedDateTime zonedDateTime = value.atStartOfDay(ZoneOffset.UTC);
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
  public void bind(DataBinder binder, LocalDate value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.DATE);
    } else {
      binder.setObject(value);
    }
  }

  @Override
  public LocalDate read(DataReader reader) throws SQLException {
    return reader.getObject(LocalDate.class);
  }

}
