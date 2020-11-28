package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.text.TextException;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Year;

/**
 * ScalarType for java.time.Year
 */
public class ScalarTypeYear extends ScalarTypeBase<Year> {

  public ScalarTypeYear() {
    super(Year.class, false, Types.INTEGER);
  }

  @Override
  public void bind(DataBinder binder, Year value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.INTEGER);
    } else {
      binder.setInt(value.getValue());
    }
  }

  @Override
  public Year read(DataReader reader) throws SQLException {
    Integer value = reader.getInt();
    return (value == null) ? null : Year.of(value);
  }

  @Override
  public Year readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return Year.of(dataInput.readInt());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Year value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeInt(value.getValue());
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Year) return ((Year) value).getValue();
    return BasicTypeConverter.toInteger(value);
  }

  @Override
  public Year toBeanType(Object value) {
    if (value instanceof Year) return (Year) value;
    if (value == null) return null;
    return Year.of(BasicTypeConverter.toInteger(value));
  }

  @Override
  public String formatValue(Year v) {
    return v.toString();
  }

  @Override
  public Year parse(String value) {
    return Year.parse(value);
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public Year convertFromMillis(long systemTimeMillis) {
    throw new TextException("Not Supported");
  }

  @Override
  public Year jsonRead(JsonParser parser) throws IOException {
    return Year.of(parser.getIntValue());
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Year value) throws IOException {
    writer.writeNumber(value.getValue());
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.INTEGER;
  }

}
