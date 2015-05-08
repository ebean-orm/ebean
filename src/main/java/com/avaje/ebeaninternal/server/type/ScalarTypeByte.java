package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebean.text.TextException;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * ScalarType for Byte.
 */
public class ScalarTypeByte extends ScalarTypeBase<Byte> {

  public ScalarTypeByte() {
    super(Byte.class, true, Types.TINYINT);
  }

  public void bind(DataBind b, Byte value) throws SQLException {
    if (value == null) {
      b.setNull(Types.TINYINT);
    } else {
      b.setByte(value);
    }
  }

  public Byte read(DataReader dataReader) throws SQLException {
    return dataReader.getByte();
  }

  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toByte(value);
  }

  public Byte toBeanType(Object value) {
    return BasicTypeConverter.toByte(value);
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, Byte value) throws IOException {
    throw new IOException("Not supported");
  }

  @Override
  public Byte jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    throw new IOException("Not supported");
  }

  public String formatValue(Byte t) {
    return t.toString();
  }

  public Byte parse(String value) {
    throw new TextException("Not supported");
  }

  public Byte convertFromMillis(long systemTimeMillis) {
    throw new TextException("Not Supported");
  }

  public boolean isDateTimeCapable() {
    return false;
  }

  public Byte readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return dataInput.readByte();
    }
  }

  public void writeData(DataOutput dataOutput, Byte val) throws IOException {

    if (val == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeByte(val);
    }
  }

}
