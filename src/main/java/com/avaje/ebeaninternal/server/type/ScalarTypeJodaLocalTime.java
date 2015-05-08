package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * ScalarType for Joda LocalTime. This maps to a JDBC Time.
 */
public class ScalarTypeJodaLocalTime extends ScalarTypeBase<LocalTime> {

  public ScalarTypeJodaLocalTime() {
    super(LocalTime.class, false, Types.TIME);
  }

  @Override
  public void bind(DataBind b, LocalTime value) throws SQLException {
    if (value == null) {
      b.setNull(Types.TIME);
    } else {
      Time sqlTime = new Time(value.getMillisOfDay());
      b.setTime(sqlTime);
    }
  }

  @Override
  public LocalTime read(DataReader dataReader) throws SQLException {

    Time sqlTime = dataReader.getTime();
    if (sqlTime == null) {
      return null;
    } else {
      return new LocalTime(sqlTime, DateTimeZone.UTC);
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof LocalTime) {
      return new Time(((LocalTime) value).getMillisOfDay());
    }
    return BasicTypeConverter.toTime(value);
  }

  @Override
  public LocalTime toBeanType(Object value) {
    if (value instanceof java.util.Date) {
      return new LocalTime(value, DateTimeZone.UTC);
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

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, LocalTime value) throws IOException {
    ctx.writeStringField(name, value.toString());
  }

  @Override
  public LocalTime jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    if (JsonToken.VALUE_NUMBER_INT == event) {
      long millis = ctx.getLongValue();
      return convertFromMillis(millis);
    } else {
      String string = ctx.getValueAsString();
      throw new RuntimeException("convert " + string);
    }
  }

  @Override
  public LocalTime convertFromMillis(long systemTimeMillis) {
    return new LocalTime(systemTimeMillis);
  }

  @Override
  public boolean isDateTimeCapable() {
    return true;
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
}
