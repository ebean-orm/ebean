package io.ebeaninternal.server.type;

import io.ebean.text.TextException;
import io.ebeaninternal.server.core.BasicTypeConverter;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Short and short.
 */
public class ScalarTypeShort extends ScalarTypeBase<Short> {

  public ScalarTypeShort() {
    super(Short.class, true, Types.SMALLINT);
  }

  @Override
  public void bind(DataBind b, Short value) throws SQLException {
    if (value == null) {
      b.setNull(Types.SMALLINT);
    } else {
      b.setShort(value);
    }
  }

  @Override
  public Short read(DataReader dataReader) throws SQLException {
    return dataReader.getShort();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toShort(value);
  }

  @Override
  public Short toBeanType(Object value) {
    return BasicTypeConverter.toShort(value);
  }

  @Override
  public String formatValue(Short v) {
    return v.toString();
  }

  @Override
  public Short parse(String value) {
    return Short.valueOf(value);
  }

  @Override
  public Short convertFromMillis(long systemTimeMillis) {
    throw new TextException("Not Supported");
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public Short readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return dataInput.readShort();
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Short value) throws IOException {

    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeShort(value);
    }
  }

  @Override
  public Short jsonRead(JsonParser parser) throws IOException {
    return parser.getShortValue();
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Short value) throws IOException {
    writer.writeNumber(value);
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.SHORT;
  }

}
