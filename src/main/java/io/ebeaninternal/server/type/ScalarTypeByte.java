package io.ebeaninternal.server.type;

import io.ebean.text.TextException;
import io.ebeaninternal.server.core.BasicTypeConverter;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Byte.
 */
public class ScalarTypeByte extends ScalarTypeBase<Byte> {

  public ScalarTypeByte() {
    super(Byte.class, true, Types.TINYINT);
  }

  @Override
  public boolean isBinaryType() {
    return true;
  }

  @Override
  public void bind(DataBind b, Byte value) throws SQLException {
    if (value == null) {
      b.setNull(Types.TINYINT);
    } else {
      b.setByte(value);
    }
  }

  @Override
  public Byte read(DataReader dataReader) throws SQLException {
    return dataReader.getByte();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toByte(value);
  }

  @Override
  public Byte toBeanType(Object value) {
    return BasicTypeConverter.toByte(value);
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Byte value) throws IOException {
    writer.writeBinary(new byte[]{value});
  }

  @Override
  public Byte jsonRead(JsonParser parser) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    parser.readBinaryValue(os);
    byte[] bytes = os.toByteArray();
    if (bytes.length == 0) {
      return null;
    } else {
      return bytes[0];
    }
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.BINARY;
  }

  @Override
  public String formatValue(Byte t) {
    return t.toString();
  }

  @Override
  public Byte parse(String value) {
    throw new TextException("Not supported");
  }

  @Override
  public Byte convertFromMillis(long systemTimeMillis) {
    throw new TextException("Not Supported");
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public Byte readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return dataInput.readByte();
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Byte val) throws IOException {

    if (val == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeByte(val);
    }
  }

}
