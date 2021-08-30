package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.text.TextException;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalTime;

/**
 * ScalarType for java.time.LocalTime stored as JDBC Time.
 */
class ScalarTypeLocalTime extends ScalarTypeBase<LocalTime> {

  ScalarTypeLocalTime() {
    super(LocalTime.class, false, Types.TIME);
  }

  ScalarTypeLocalTime(int jdbcTtype) {
    super(LocalTime.class, false, jdbcTtype);
  }

  @Override
  public void bind(DataBinder binder, LocalTime value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.TIME);
    } else {
      binder.setTime(Time.valueOf(value));
    }
  }

  @Override
  public LocalTime read(DataReader reader) throws SQLException {
    Time time = reader.getTime();
    return (time == null) ? null : time.toLocalTime();
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Time) return value;
    return Time.valueOf((LocalTime) value);
  }

  @Override
  public LocalTime toBeanType(Object value) {
    if (value instanceof LocalTime) return (LocalTime) value;
    if (value == null) return null;
    return BasicTypeConverter.toTime(value).toLocalTime();
  }

  @Override
  public LocalTime readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return LocalTime.ofNanoOfDay(dataInput.readLong());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, LocalTime value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeLong(value.toNanoOfDay());
    }
  }

  @Override
  public String formatValue(LocalTime v) {
    return v.toString();
  }

  @Override
  public LocalTime parse(String value) {
    return LocalTime.parse(value);
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public LocalTime convertFromMillis(long systemTimeMillis) {
    throw new TextException("Not Supported");
  }

  @Override
  public LocalTime jsonRead(JsonParser parser) throws IOException {
    return LocalTime.parse(parser.getValueAsString());
  }

  @Override
  public void jsonWrite(JsonGenerator writer, LocalTime value) throws IOException {
    writer.writeString(value.toString());
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.KEYWORD;
  }

}
