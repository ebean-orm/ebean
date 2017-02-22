package io.ebeaninternal.server.type;

import io.ebeaninternal.server.core.BasicTypeConverter;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for java.sql.Date.
 */
public class ScalarTypeDate extends ScalarTypeBaseDate<java.sql.Date> {

  public ScalarTypeDate() {
    super(Date.class, true, Types.DATE);
  }

  @Override
  public long convertToMillis(Date value) {
    return value.getTime();
  }

  @Override
  public Date convertFromDate(Date date) {
    return date;
  }

  @Override
  public Date convertToDate(Date t) {
    return t;
  }

  @Override
  public void bind(DataBind b, java.sql.Date value) throws SQLException {
    if (value == null) {
      b.setNull(Types.DATE);
    } else {
      b.setDate(value);
    }
  }

  @Override
  public java.sql.Date read(DataReader dataReader) throws SQLException {
    return dataReader.getDate();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toDate(value);
  }

  @Override
  public java.sql.Date toBeanType(Object value) {
    return BasicTypeConverter.toDate(value);
  }

}
