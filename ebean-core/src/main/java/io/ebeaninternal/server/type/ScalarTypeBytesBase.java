package io.ebeaninternal.server.type;

import io.ebean.text.TextException;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

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

  @Override
  public boolean isBinaryType() {
    return true;
  }

  public byte[] convertToBytes(Object value) {
    return (byte[]) value;
  }

  @Override
  public void bind(DataBind b, byte[] value) throws SQLException {
    if (value == null) {
      b.setNull(jdbcType);
    } else {
      b.setBytes(value);
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    return value;
  }

  @Override
  public byte[] toBeanType(Object value) {
    return (byte[]) value;
  }

  @Override
  public String formatValue(byte[] t) {
    throw new TextException("Not supported");
  }

  @Override
  public byte[] parse(String value) {
    throw new TextException("Not supported");
  }

  @Override
  public byte[] convertFromMillis(long systemTimeMillis) {
    throw new TextException("Not supported");
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
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

  @Override
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

  @Override
  public void jsonWrite(JsonGenerator writer, byte[] value) throws IOException {
    writer.writeBinary(value);
  }

  @Override
  public byte[] jsonRead(JsonParser parser) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream(500);
    parser.readBinaryValue(out);
    return out.toByteArray();
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.BINARY;
  }
}
