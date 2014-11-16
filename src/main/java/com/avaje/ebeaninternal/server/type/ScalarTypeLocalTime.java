package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.text.TextException;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalTime;
import java.time.Year;

/**
 * ScalarType for java.time.Year
 */
public class ScalarTypeLocalTime extends ScalarTypeBase<LocalTime> {

  public ScalarTypeLocalTime() {
    super(LocalTime.class, true, Types.BIGINT);
  }

  @Override
  public void bind(DataBind bind, LocalTime value) throws SQLException {
    if (value == null) {
      bind.setNull(Types.BIGINT);
    } else {
      bind.setLong(value.toNanoOfDay());
    }
  }

  @Override
  public LocalTime read(DataReader dataReader) throws SQLException {
    return LocalTime.ofNanoOfDay(dataReader.getLong());
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
  public Object toJdbcType(Object value) {
    if (value instanceof  Long) return value;
    return ((LocalTime)value).toNanoOfDay();
  }

  @Override
  public LocalTime toBeanType(Object value) {
    if (value instanceof LocalTime) return (LocalTime) value;
    return LocalTime.ofNanoOfDay(BasicTypeConverter.toLong(value));
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
  public LocalTime jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return LocalTime.parse(ctx.getValueAsString());
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, LocalTime value) throws IOException {
    ctx.writeStringField(name, value.toString());
  }
}
