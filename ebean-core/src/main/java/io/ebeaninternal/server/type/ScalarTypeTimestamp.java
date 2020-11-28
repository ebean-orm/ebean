package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

import static io.ebeaninternal.server.type.IsoJsonDateTimeParser.formatIso;

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
    return formatIso(value.toInstant());
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
  public Timestamp convertFromInstant(Instant ts) {
    return Timestamp.from(ts);
  }

  @Override
  public Timestamp convertToTimestamp(Timestamp t) {
    return t;
  }

  @Override
  public void bind(DataBinder binder, Timestamp value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.TIMESTAMP);
    } else {
      binder.setTimestamp(value);
    }
  }

  @Override
  public Timestamp read(DataReader reader) throws SQLException {
    return reader.getTimestamp();
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
