package io.ebean.core.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebean.config.JsonConfig;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarTypeBase;

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

  public ScalarTypeBaseDate(JsonConfig.Date mode, Class<T> type, boolean jdbcNative, int jdbcType) {
    super(type, jdbcNative, jdbcType);
    this.mode = mode;
  }

  /**
   * Convert the value to ISO8601 format.
   */
  protected abstract String toIsoFormat(T value);

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
    final Date date = reader.getDate();
    return date == null ? null : convertFromDate(date);
  }

  @Override
  public String formatValue(T value) {
    final Date date = convertToDate(value);
    return Long.toString(date.getTime());
  }

  @Override
  public T parse(String value) {
    try {
      return convertFromDate(new Date(Long.parseLong(value)));
    } catch (NumberFormatException e) {
      return convertFromDate(Date.valueOf(value));
    }
  }

  public T convertFromMillis(long systemTimeMillis) {
    return convertFromDate(new Date(systemTimeMillis));
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

  @Override
  public DocPropertyType docType() {
    return DocPropertyType.DATE;
  }

  @Override
  public T readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return convertFromDate(new Date(dataInput.readLong()));
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, T value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeLong(convertToDate(value).getTime());
    }
  }

}
