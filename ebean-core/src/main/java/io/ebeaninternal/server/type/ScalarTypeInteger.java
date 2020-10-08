package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.text.TextException;
import io.ebeaninternal.server.core.BasicTypeConverter;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Integer and int.
 */
public class ScalarTypeInteger extends ScalarTypeBase<Integer> {

  public static ScalarTypeInteger INSTANCE = new ScalarTypeInteger();

  private ScalarTypeInteger() {
    super(Integer.class, true, Types.INTEGER);
  }

  @Override
  public void bind(DataBind b, Integer value) throws SQLException {
    if (value == null) {
      b.setNull(Types.INTEGER);
    } else {
      b.setInt(value);
    }
  }

  @Override
  public Integer read(DataReader dataReader) throws SQLException {
    return dataReader.getInt();
  }

  @Override
  public Integer readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return dataInput.readInt();
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Integer value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeInt(value);
    }
  }

  @Override
  public long asVersion(Integer value) {
    return value.longValue();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toInteger(value);
  }

  @Override
  public Integer toBeanType(Object value) {
    return BasicTypeConverter.toInteger(value);
  }

  @Override
  public String formatValue(Integer v) {
    return v.toString();
  }

  @Override
  public Integer parse(String value) {
    return Integer.valueOf(value);
  }

  @Override
  public Integer convertFromMillis(long systemTimeMillis) {
    throw new TextException("Not Supported");
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public Integer jsonRead(JsonParser parser) throws IOException {
    return parser.getIntValue();
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Integer value) throws IOException {
    writer.writeNumber(value);
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.INTEGER;
  }

}
