package io.ebeaninternal.server.type;

import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

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

  public ScalarTypeBaseDate(Class<T> type, boolean jdbcNative, int jdbcType) {
    super(type, jdbcNative, jdbcType);
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
  public void bind(DataBind b, T value) throws SQLException {
    if (value == null) {
      b.setNull(Types.DATE);
    } else {
      b.setDate(convertToDate(value));
    }
  }

  @Override
  public T read(DataReader dataReader) throws SQLException {

    Date ts = dataReader.getDate();
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
    writer.writeNumber(convertToMillis(value));
  }

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
