package io.ebean.core.type;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonReader.Token;
import io.avaje.json.JsonWriter;
import io.ebean.config.JsonConfig;

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
   * Convert to the value from a Instant.
   */
  public abstract T convertFromInstant(Instant ts);

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

  /**
   * Convert the value to ISO8601 format.
   */
  protected T fromJsonISO8601(String value) {
    return convertFromInstant(ScalarTypeUtils.parseInstant(value));
  }

  @Override
  public void bind(DataBinder binder, T value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.TIMESTAMP);
    } else {
      binder.setTimestamp(convertToTimestamp(value));
    }
  }

  @Override
  public T read(DataReader reader) throws SQLException {
    Timestamp ts = reader.getTimestamp();
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
    return ScalarTypeUtils.toDecimal(epochSecs, nanos);
  }

  @Override
  public T jsonRead(JsonReader parser) throws IOException {
    Token token = parser.currentToken();
    if (token == Token.NUMBER) {
      return readNumber(parser.readDecimal());
    }
    if (token == Token.STRING) {
      return fromStringValue(parser.readString());
    }

    String raw = parser.readRaw();
    if (raw == null || "null".equals(raw)) {
      return null;
    }
    if (raw.length() > 1 && raw.charAt(0) == '"' && raw.charAt(raw.length() - 1) == '"') {
      return fromStringValue(raw.substring(1, raw.length() - 1));
    }
    return readNumber(new BigDecimal(raw));
  }

  private T fromStringValue(String value) {
    if (value.indexOf('-') == -1 && Character.isDigit(value.charAt(0))) {
      return readNumber(new BigDecimal(value));
    }
    return fromJsonISO8601(value);
  }

  private T readNumber(BigDecimal value) {
    if (value.scale() <= 0) {
      return convertFromMillis(value.longValue());
    }
    Timestamp timestamp = ScalarTypeUtils.toTimestamp(value);
    return convertFromTimestamp(timestamp);
  }

  @Override
  public void jsonWrite(JsonWriter writer, T value) throws IOException {
    switch (mode) {
      case ISO8601: {
        writer.value(toJsonISO8601(value));
        break;
      }
      case NANOS: {
        writer.value(toJsonNanos(value));
        break;
      }
      default: {
        writer.value(convertToMillis(value));
      }
    }
  }

  @Override
  public DocPropertyType docType() {
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
