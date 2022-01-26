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
 * ScalarType for java.time.LocalDate. This maps to a LocalDate. Not all drivers/platforms may support this.
 */
final class ScalarTypeLocalDateNative extends ScalarTypeLocalDate {

  ScalarTypeLocalDateNative(JsonConfig.Date mode) {
    super(mode);
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
