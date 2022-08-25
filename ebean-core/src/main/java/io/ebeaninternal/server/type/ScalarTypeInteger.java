package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarTypeBase;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Integer and int.
 */
final class ScalarTypeInteger extends ScalarTypeBase<Integer> {

  ScalarTypeInteger() {
    super(Integer.class, true, Types.INTEGER);
  }

  @Override
  public void bind(DataBinder binder, Integer value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.INTEGER);
    } else {
      binder.setInt(value);
    }
  }

  @Override
  public Integer read(DataReader reader) throws SQLException {
    return reader.getInt();
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
  public String format(Object value) {
    return String.valueOf(value);
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
  public Integer jsonRead(JsonParser parser) throws IOException {
    return parser.getIntValue();
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Integer value) throws IOException {
    writer.writeNumber(value);
  }

  @Override
  public DocPropertyType docType() {
    return DocPropertyType.INTEGER;
  }

}
