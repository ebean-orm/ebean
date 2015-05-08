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
 * ScalarType for String.
 */
public class ScalarTypeString extends ScalarTypeBase<String> {

  public ScalarTypeString() {
    super(String.class, true, Types.VARCHAR);
  }

  @Override
  public void bind(DataBind b, String value) throws SQLException {
    if (value == null) {
      b.setNull(Types.VARCHAR);
    } else {
      b.setString(value);
    }
  }

  @Override
  public String read(DataReader dataReader) throws SQLException {
    return dataReader.getString();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toString(value);
  }

  @Override
  public String toBeanType(Object value) {
    return BasicTypeConverter.toString(value);
  }

  @Override
  public String formatValue(String t) {
    return t;
  }

  @Override
  public String parse(String value) {
    return value;
  }

  @Override
  public String convertFromMillis(long systemTimeMillis) {
    return String.valueOf(systemTimeMillis);
  }

  @Override
  public boolean isDateTimeCapable() {
    return true;
  }

  @Override
  public String readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return dataInput.readUTF();
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, String value) throws IOException {

    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeUTF(value);
    }
  }

  @Override
  public String jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return ctx.getValueAsString();
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, String value) throws IOException {
    ctx.writeStringField(name, (String) value);
  }
}
