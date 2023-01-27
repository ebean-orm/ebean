package io.ebean.joda.time;

import java.sql.SQLException;
import java.sql.Types;

import org.joda.time.LocalDate;

import io.ebean.config.JsonConfig;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;

/**
 * ScalarType for Joda LocalDate. This maps to a LocalDate. Not all drivers/platforms may support this.
 */
final class ScalarTypeJodaLocalDateNative extends ScalarTypeJodaLocalDate{

  ScalarTypeJodaLocalDateNative(JsonConfig.Date mode) {
    super(mode);
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
