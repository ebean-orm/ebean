package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Float and float.
 */
public class ScalarTypeFloat extends ScalarTypeBase<Float> {

  public ScalarTypeFloat() {
    super(Float.class, true, Types.REAL);
  }

  @Override
  public void bind(DataBinder binder, Float value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.REAL);
    } else {
      binder.setFloat(value);
    }
  }

  @Override
  public Float read(DataReader reader) throws SQLException {
    return reader.getFloat();
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
    return (float) systemTimeMillis;
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
      return dataInput.readFloat();
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
  public Float jsonRead(JsonParser parser) throws IOException {
    return parser.getFloatValue();
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Float value) throws IOException {
    writer.writeNumber(value);
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.FLOAT;
  }

}
