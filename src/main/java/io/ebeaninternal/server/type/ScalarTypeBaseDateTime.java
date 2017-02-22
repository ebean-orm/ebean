package io.ebeaninternal.server.type;

import io.ebean.config.JsonConfig;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

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

  @Override
  public long asVersion(T value) {
    return convertToMillis(value);
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
  @Override
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

  @Override
  public void bind(DataBind b, T value) throws SQLException {
    if (value == null) {
      b.setNull(Types.TIMESTAMP);
    } else {
      b.setTimestamp(convertToTimestamp(value));
    }
  }

  @Override
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
  public T jsonRead(JsonParser parser) throws IOException {

    switch (parser.getCurrentToken()) {
      case VALUE_NUMBER_INT: {
        return convertFromMillis(parser.getLongValue());
      }
      case VALUE_NUMBER_FLOAT: {
        BigDecimal value = parser.getDecimalValue();
        Timestamp timestamp = DecimalUtils.toTimestamp(value);
        return convertFromTimestamp(timestamp);
      }
      default: {
        String jsonDateTime = parser.getText();
        return convertFromTimestamp(dateTimeParser.parse(jsonDateTime));
      }
    }
  }

  @Override
  public void jsonWrite(JsonGenerator writer, T value) throws IOException {

    switch (mode) {
      case ISO8601: {
        writer.writeString(toJsonISO8601(value));
        break;
      }
      case NANOS: {
        writer.writeNumber(toJsonNanos(value));
        break;
      }
      default: {
        writer.writeNumber(convertToMillis(value));
      }
    }
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.DATETIME;
  }

  @Override
  public String formatValue(T value) {
    // format all timestamps into epoch millis
    long epochMillis = convertToMillis(value);
    return Long.toString(epochMillis);
  }

  @Override
  public T parse(String value) {
    try {
      long epochMillis = Long.parseLong(value);
      return convertFromMillis(epochMillis);
    } catch (NumberFormatException e) {
      return convertFromTimestamp(Timestamp.valueOf(value));
    }
  }

  @Override
  public boolean isDateTimeCapable() {
    return true;
  }

  @Override
  public T readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      long val = dataInput.readLong();
      Timestamp ts = new Timestamp(val);
      return convertFromTimestamp(ts);
    }
  }

  @Override
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
