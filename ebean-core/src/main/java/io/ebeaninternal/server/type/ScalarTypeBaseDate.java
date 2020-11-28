package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebean.config.JsonConfig;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Base class for Date types.
 */
public abstract class ScalarTypeBaseDate<T> extends ScalarTypeBase<T> {

  protected final JsonConfig.Date mode;

  ScalarTypeBaseDate(JsonConfig.Date mode, Class<T> type, boolean jdbcNative, int jdbcType) {
    super(type, jdbcNative, jdbcType);
    this.mode = mode;
  }

  /**
   * Convert the target value to millis.
   */
  public abstract long convertToMillis(T value);

  /**
   * Convert to java.sql.Date from the target Date type.
   */
  public abstract java.sql.Date convertToDate(T t);

  /**
   * Convert from java.sql.Date to the target Date type.
   */
  public abstract T convertFromDate(java.sql.Date ts);

  @Override
  public void bind(DataBinder binder, T value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.DATE);
    } else {
      binder.setDate(convertToDate(value));
    }
  }

  @Override
  public T read(DataReader reader) throws SQLException {
    Date ts = reader.getDate();
    return ts == null ? null : convertFromDate(ts);
  }

  @Override
  public String formatValue(T t) {
    Date date = convertToDate(t);
    // format all dates into epoch millis
    long epochMillis = date.getTime();
    return Long.toString(epochMillis);
  }

  @Override
  public T parse(String value) {
    try {
      long epochMillis = Long.parseLong(value);
      return convertFromDate(new Date(epochMillis));
    } catch (NumberFormatException e) {
      Date date = Date.valueOf(value);
      return convertFromDate(date);
    }
  }

  @Override
  public T convertFromMillis(long systemTimeMillis) {
    Date ts = new Date(systemTimeMillis);
    return convertFromDate(ts);
  }

  @Override
  public boolean isDateTimeCapable() {
    return true;
  }

  @Override
  public T jsonRead(JsonParser parser) throws IOException {
    if (JsonToken.VALUE_NUMBER_INT == parser.getCurrentToken()) {
      return convertFromMillis(parser.getLongValue());
    } else {
      return convertFromDate(Date.valueOf(parser.getText()));
    }
  }

  @Override
  public void jsonWrite(JsonGenerator writer, T value) throws IOException {
    if (mode == JsonConfig.Date.ISO8601) {
      writer.writeString(toIsoFormat(value));
    } else {
      writer.writeNumber(convertToMillis(value));
    }
  }

  /**
   * Convert the value to ISO8601 format.
   */
  protected abstract String toIsoFormat(T value);

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.DATE;
  }

  @Override
  public T readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      long val = dataInput.readLong();
      Date date = new Date(val);
      return convertFromDate(date);
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, T value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      Date date = convertToDate(value);
      dataOutput.writeLong(date.getTime());
    }
  }

}
