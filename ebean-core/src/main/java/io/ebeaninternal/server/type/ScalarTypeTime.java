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
import java.sql.Time;
import java.sql.Types;

/**
 * ScalarType for java.sql.Time.
 */
public class ScalarTypeTime extends ScalarTypeBase<Time> {

  public ScalarTypeTime() {
    super(Time.class, true, Types.TIME);
  }

  @Override
  public void bind(DataBinder binder, Time value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.TIME);
    } else {
      binder.setTime(value);
    }
  }

  @Override
  public Time read(DataReader reader) throws SQLException {
    return reader.getTime();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toTime(value);
  }

  @Override
  public Time toBeanType(Object value) {
    return BasicTypeConverter.toTime(value);
  }

  @Override
  public String formatValue(Time v) {
    return v.toString();
  }

  @Override
  public Time parse(String value) {
    return Time.valueOf(value);
  }

  @Override
  public Time convertFromMillis(long systemTimeMillis) {
    return new Time(systemTimeMillis);
  }

  @Override
  public boolean isDateTimeCapable() {
    return true;
  }

  @Override
  public Time readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      String val = dataInput.readUTF();
      return parse(val);
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Time value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeUTF(format(value));
    }
  }

  @Override
  public Time jsonRead(JsonParser parser) throws IOException {
    return parse(parser.getValueAsString());
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Time value) throws IOException {
    writer.writeString(format(value));
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.KEYWORD;
  }

}
