package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.JsonConfig;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

/**
 * Base type for DateTime types.
 */
public abstract class ScalarTypeBaseDateTime<T> extends ScalarTypeBase<T> {


  protected final DateTimeJsonParser dateTimeParser = new DateTimeJsonParser();

  protected final JsonConfig.DateTime mode;

  public ScalarTypeBaseDateTime(JsonConfig.DateTime mode, Class<T> type, boolean jdbcNative, int jdbcType) {
    super(type, jdbcNative, jdbcType);
    this.mode = mode;
  }

  /**
   * Convert the value to a Timestamp.
   */
  public abstract Timestamp convertToTimestamp(T t);

  /**
   * Convert to the value from a Timestamp.
   */
  public abstract T convertFromTimestamp(Timestamp ts);

  /**
   * Convert from epoch millis to the value.
   */
  public abstract T convertFromMillis(long systemTimeMillis);

  /**
   * Convert from the value to epoch millis.
   */
  public abstract long convertToMillis(T value);

  /**
   * Convert the value to time with nanos format.
   */
  protected abstract String toJsonNanos(T value);

  /**
   * Convert the value to ISO8601 format.
   */
  protected abstract String toJsonISO8601(T value);

  public void bind(DataBind b, T value) throws SQLException {
    if (value == null) {
      b.setNull(Types.TIMESTAMP);
    } else {
      b.setTimestamp(convertToTimestamp(value));
    }
  }

  public T read(DataReader dataReader) throws SQLException {

    Timestamp ts = dataReader.getTimestamp();
    if (ts == null) {
      return null;
    } else {
      return convertFromTimestamp(ts);
    }
  }

  /**
   * Helper method that given epoch seconds and nanos return a JSON nanos formatted string.
   */
  protected String toJsonNanos(long epochSecs, int nanos) {
    return DecimalUtils.toDecimal(epochSecs, nanos);
  }

  @Override
  public T jsonRead(JsonParser ctx, JsonToken event) throws IOException {

    switch (event) {
      case VALUE_NUMBER_INT: {
        return convertFromMillis(ctx.getLongValue());
      }
      case VALUE_NUMBER_FLOAT: {
        BigDecimal value = ctx.getDecimalValue();
        Timestamp timestamp = DecimalUtils.toTimestamp(value);
        return convertFromTimestamp(timestamp);
      }
      default: {
        String jsonDateTime = ctx.getText();
        return convertFromTimestamp(dateTimeParser.parse(jsonDateTime));        
      }
    }
  }

  @Override
  public void jsonWrite(JsonGenerator generator, String name, T value) throws IOException {

    switch (mode) {
      case ISO8601: {
        generator.writeFieldName(name);
        generator.writeString(toJsonISO8601(value));
        break;
      }
      case NANOS: {
        generator.writeFieldName(name);
        generator.writeNumber(toJsonNanos(value));
        break;
      }
      default: {
        generator.writeNumberField(name, convertToMillis(value));
      }
    }
  }

  public String formatValue(T t) {
    Timestamp ts = convertToTimestamp(t);
    return ts.toString();
  }

  public T parse(String value) {
    Timestamp ts = Timestamp.valueOf(value);
    return convertFromTimestamp(ts);
  }


  public boolean isDateTimeCapable() {
    return true;
  }

  public T readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      long val = dataInput.readLong();
      Timestamp ts = new Timestamp(val);
      return convertFromTimestamp(ts);
    }
  }

  public void writeData(DataOutput dataOutput, T value) throws IOException {

    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      Timestamp ts = convertToTimestamp(value);
      dataOutput.writeLong(ts.getTime());
    }
  }

}
