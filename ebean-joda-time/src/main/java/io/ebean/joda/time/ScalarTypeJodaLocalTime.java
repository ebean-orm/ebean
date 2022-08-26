package io.ebean.joda.time;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarTypeBase;
import io.ebean.core.type.BasicTypeConverter;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;

/**
 * ScalarType for Joda LocalTime. This maps to a JDBC Time.
 */
class ScalarTypeJodaLocalTime extends ScalarTypeBase<LocalTime> {

  ScalarTypeJodaLocalTime() {
    super(LocalTime.class, false, Types.TIME);
  }

  @Override
  public void bind(DataBinder binder, LocalTime value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.TIME);
    } else {
      binder.setTime(new Time(value.toDateTimeToday().getMillis()));
    }
  }

  @Override
  public LocalTime read(DataReader reader) throws SQLException {
    Time sqlTime = reader.getTime();
    if (sqlTime == null) {
      return null;
    } else {
      return new LocalTime(sqlTime, DateTimeZone.getDefault());
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof LocalTime) {
      LocalTime lt = (LocalTime) value;
      return new Time(lt.toDateTimeToday().getMillis());
    }
    return BasicTypeConverter.toTime(value);
  }

  @Override
  public LocalTime toBeanType(Object value) {
    if (value instanceof java.util.Date) {
      return new LocalTime(value, DateTimeZone.getDefault());
    }
    return (LocalTime) value;
  }

  @Override
  public String formatValue(LocalTime v) {
    return v.toString();
  }

  @Override
  public LocalTime parse(String value) {
    return new LocalTime(value);
  }

  public LocalTime convertFromMillis(long systemTimeMillis) {
    return new LocalTime(systemTimeMillis, DateTimeZone.getDefault());
  }

  @Override
  public LocalTime readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      String val = dataInput.readUTF();
      return parse(val);
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, LocalTime value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeUTF(format(value));
    }
  }

  @Override
  public void jsonWrite(JsonGenerator writer, LocalTime value) throws IOException {
    writer.writeString(value.toString());
  }

  @Override
  public LocalTime jsonRead(JsonParser parser) throws IOException {
    if (JsonToken.VALUE_NUMBER_INT == parser.getCurrentToken()) {
      return convertFromMillis(parser.getLongValue());
    } else {
      return parse(parser.getValueAsString());
    }
  }

  @Override
  public DocPropertyType docType() {
    return DocPropertyType.KEYWORD;
  }
}
