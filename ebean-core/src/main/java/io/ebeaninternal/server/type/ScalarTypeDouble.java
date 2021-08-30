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
 * ScalarType for Double and double.
 */
final class ScalarTypeDouble extends ScalarTypeBase<Double> {

  ScalarTypeDouble() {
    super(Double.class, true, Types.DOUBLE);
  }

  @Override
  public void bind(DataBinder binder, Double value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.DOUBLE);
    } else {
      binder.setDouble(value);
    }
  }

  @Override
  public Double read(DataReader dataReader) throws SQLException {
    return dataReader.getDouble();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toDouble(value);
  }

  @Override
  public Double toBeanType(Object value) {
    return BasicTypeConverter.toDouble(value);
  }

  @Override
  public String formatValue(Double t) {
    return t.toString();
  }

  @Override
  public Double parse(String value) {
    return Double.valueOf(value);
  }

  @Override
  public Double convertFromMillis(long systemTimeMillis) {
    return (double) systemTimeMillis;
  }

  @Override
  public boolean isDateTimeCapable() {
    return true;
  }

  @Override
  public Double readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return dataInput.readDouble();
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Double value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeDouble(value);
    }
  }

  @Override
  public Double jsonRead(JsonParser parser) throws IOException {
    return parser.getDoubleValue();
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Double value) throws IOException {
    writer.writeNumber(value);
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.DOUBLE;
  }

}
