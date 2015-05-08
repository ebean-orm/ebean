package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;

import com.avaje.ebean.config.JsonConfig;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for java.sql.Timestamp.
 */
public class ScalarTypeTimestamp extends ScalarTypeBaseDateTime<Timestamp> {

  public ScalarTypeTimestamp(JsonConfig.DateTime mode) {
    super(mode, Timestamp.class, true, Types.TIMESTAMP);
  }

  @Override
  protected String toJsonNanos(Timestamp value) {
    return String.valueOf(value.getTime());
  }

  @Override
  protected String toJsonISO8601(Timestamp value) {
    return dateTimeParser.format(value);
  }

  @Override
  public long convertToMillis(Timestamp value) {
    return value.getTime();
  }

  @Override
  public Timestamp convertFromMillis(long systemTimeMillis) {
    return new Timestamp(systemTimeMillis);
  }

  @Override
  public Timestamp convertFromTimestamp(Timestamp ts) {
    return ts;
  }

  @Override
  public Timestamp convertToTimestamp(Timestamp t) {
    return t;
  }



  @Override
  public void bind(DataBind b, Timestamp value) throws SQLException {
    if (value == null) {
      b.setNull(Types.TIMESTAMP);
    } else {
      b.setTimestamp(value);
    }
  }

  @Override
  public Timestamp read(DataReader dataReader) throws SQLException {
    return dataReader.getTimestamp();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toTimestamp(value);
  }

  @Override
  public Timestamp toBeanType(Object value) {
    return BasicTypeConverter.toTimestamp(value);
  }
}
