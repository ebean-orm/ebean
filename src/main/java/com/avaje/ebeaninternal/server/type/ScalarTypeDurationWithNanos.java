package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.text.TextException;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;

/**
 * ScalarType for java.time.Duration (with Nanos precision).
 * <p>
 *  Stored in the DB as DECIMAL value.
 * </p>
 */
public class ScalarTypeDurationWithNanos extends ScalarTypeDuration {

  public ScalarTypeDurationWithNanos() {
    super(Types.DECIMAL);
  }

  @Override
  public void bind(DataBind bind, Duration value) throws SQLException {
    if (value == null) {
      bind.setNull(Types.DECIMAL);
    } else {
      bind.setBigDecimal(convertToBigDecimal(value));
    }
  }

  @Override
  public Duration read(DataReader dataReader) throws SQLException {
    return convertFromBigDecimal(dataReader.getBigDecimal());
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof  BigDecimal) return value;
    return convertToBigDecimal((Duration)value);
  }

  @Override
  public Duration toBeanType(Object value) {
    if (value instanceof Duration) return (Duration) value;
    return convertFromBigDecimal(BasicTypeConverter.toBigDecimal(value));
  }

}
