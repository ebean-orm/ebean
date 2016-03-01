package com.avaje.ebeaninternal.server.type;

import com.avaje.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.avaje.ebean.text.TextException;
import com.avaje.ebean.text.json.JsonWriter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Base type for binary types.
 */
public abstract class ScalarTypeBytesBase extends ScalarTypeBase<byte[]> {

  protected ScalarTypeBytesBase(boolean jdbcNative, int jdbcType) {
    super(byte[].class, jdbcNative, jdbcType);
  }

  public byte[] convertToBytes(Object value) {
    return (byte[]) value;
  }

  public void bind(DataBind b, byte[] value) throws SQLException {
    if (value == null) {
      b.setNull(jdbcType);
    } else {
      b.setBytes(value);
    }
  }

  public Object toJdbcType(Object value) {
    return value;
  }

  public byte[] toBeanType(Object value) {
    return (byte[]) value;
  }

  @Override
  public void jsonWrite(JsonWriter writer, String name, byte[] value) throws IOException {
    writer.writeBinaryField(name, value);
  }

  @Override
  public byte[] jsonRead(JsonParser parser, JsonToken event) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream(500);
    parser.readBinaryValue(out);
    return out.toByteArray();
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.BINARY;
  }

  public String formatValue(byte[] t) {
    throw new TextException("Not supported");
  }

  public byte[] parse(String value) {
    throw new TextException("Not supported");
  }

  public byte[] convertFromMillis(long systemTimeMillis) {
    throw new TextException("Not supported");
  }

  public boolean isDateTimeCapable() {
    return false;
  }

  public byte[] readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      int len = dataInput.readInt();
      byte[] buf = new byte[len];
      dataInput.readFully(buf, 0, buf.length);
      return buf;
    }
  }

  public void writeData(DataOutput dataOutput, byte[] v) throws IOException {
    if (v == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      byte[] bytes = convertToBytes(v);
      dataOutput.writeInt(bytes.length);
      dataOutput.write(bytes);
    }
  }

}
