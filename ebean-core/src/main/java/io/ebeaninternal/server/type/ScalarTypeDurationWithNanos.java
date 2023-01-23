package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.BasicTypeConverter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;

/**
 * ScalarType for java.time.Duration (with Nanos precision).
 * <p>
 * Stored in the DB as DECIMAL value.
 * </p>
 */
final class ScalarTypeDurationWithNanos extends ScalarTypeDuration {

  ScalarTypeDurationWithNanos() {
    super(Types.DECIMAL);
  }

  @Override
  public void bind(DataBinder binder, Duration value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.DECIMAL);
    } else {
      binder.setBigDecimal(convertToBigDecimal(value));
    }
  }

  @Override
  public Duration read(DataReader reader) throws SQLException {
    return convertFromBigDecimal(reader.getBigDecimal());
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof BigDecimal) return value;
    return convertToBigDecimal((Duration) value);
  }

  @Override
  public Duration toBeanType(Object value) {
    if (value instanceof Duration) return (Duration) value;
    if (value == null) return null;
    return convertFromBigDecimal(BasicTypeConverter.toBigDecimal(value));
  }

}
