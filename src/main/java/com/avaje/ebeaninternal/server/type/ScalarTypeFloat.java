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
 * ScalarType for Float and float.
 */
public class ScalarTypeFloat extends ScalarTypeBase<Float> {

  public ScalarTypeFloat() {
    super(Float.class, true, Types.REAL);
  }

  @Override
  public void bind(DataBind b, Float value) throws SQLException {
    if (value == null) {
      b.setNull(Types.REAL);
    } else {
      b.setFloat(value.floatValue());
    }
  }

  @Override
  public Float read(DataReader dataReader) throws SQLException {
    return dataReader.getFloat();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toFloat(value);
  }

  @Override
  public Float toBeanType(Object value) {
    return BasicTypeConverter.toFloat(value);
  }

  @Override
  public String formatValue(Float t) {
    return t.toString();
  }

  @Override
  public Float parse(String value) {
    return Float.valueOf(value);
  }

  @Override
  public Float convertFromMillis(long systemTimeMillis) {
    return Float.valueOf(systemTimeMillis);
  }

  @Override
  public boolean isDateTimeCapable() {
    return true;
  }

  @Override
  public Float readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      float val = dataInput.readFloat();
      return Float.valueOf(val);
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Float value) throws IOException {

    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeFloat(value);
    }
  }

  @Override
  public Float jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return ctx.getFloatValue();
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, Float value) throws IOException {
    ctx.writeNumberField(name, (Float) value);
  }
}
