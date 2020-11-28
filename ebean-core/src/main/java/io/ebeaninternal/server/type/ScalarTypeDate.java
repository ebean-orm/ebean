package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for java.sql.Date.
 */
public class ScalarTypeDate extends ScalarTypeBaseDate<java.sql.Date> {

  public ScalarTypeDate(JsonConfig.Date mode) {
    super(mode, Date.class, true, Types.DATE);
  }

  @Override
  protected String toIsoFormat(Date value) {
    return value.toLocalDate().toString();
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
  public void bind(DataBinder binder, java.sql.Date value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.DATE);
    } else {
      binder.setDate(value);
    }
  }

  @Override
  public java.sql.Date read(DataReader reader) throws SQLException {
    return reader.getDate();
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
