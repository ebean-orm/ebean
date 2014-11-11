package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * ScalarType for Double and double.
 */
public class ScalarTypeDouble extends ScalarTypeBase<Double> {

  public ScalarTypeDouble() {
    super(Double.class, true, Types.DOUBLE);
  }

  @Override
  public void bind(DataBind b, Double value) throws SQLException {
    if (value == null) {
      b.setNull(Types.DOUBLE);
    } else {
      b.setDouble(value.doubleValue());
    }
  }

  @Override
  public Double read(DataReader dataReader) throws SQLException {
    return dataReader.getDouble();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toDouble(value);
  }

  @Override
  public Double toBeanType(Object value) {
    return BasicTypeConverter.toDouble(value);
  }

  @Override
  public String formatValue(Double t) {
    return t.toString();
  }

  @Override
  public Double parse(String value) {
    return Double.valueOf(value);
  }

  @Override
  public Double parseDateTime(long systemTimeMillis) {
    return Double.valueOf(systemTimeMillis);
  }

  @Override
  public boolean isDateTimeCapable() {
    return true;
  }

  @Override
  public Object readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      double val = dataInput.readDouble();
      return Double.valueOf(val);
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Object v) throws IOException {

    Double value = (Double) v;
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeDouble(value.doubleValue());
    }
  }

  @Override
  public Object jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return ctx.getDoubleValue();
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, Object value) throws IOException {
    ctx.writeNumberField(name, (Double) value);
  }
}
