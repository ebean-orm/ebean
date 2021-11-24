package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.config.JsonConfig;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

import static io.ebeaninternal.server.type.IsoJsonDateTimeParser.parseIso;

/**
 * Base type for DateTime types.
 */
abstract class ScalarTypeBaseDateTime<T> extends ScalarTypeBase<T> {

  protected final JsonConfig.DateTime mode;
  protected final boolean isLocal;

  ScalarTypeBaseDateTime(JsonConfig.DateTime mode, Class<T> type, boolean jdbcNative, int jdbcType, boolean isLocal) {
    super(type, jdbcNative, jdbcType);
    this.mode = mode;
    this.isLocal = isLocal;
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

  protected T fromJsonNanos(long seconds, int nanoseconds) {
    Timestamp ts = new Timestamp(seconds * 1000);
    ts.setNanos(nanoseconds);
    return convertFromTimestamp(ts);
  }
  
  /**
   * Convert the value to ISO8601 format.
   */
  protected abstract String toJsonISO8601(T value);

  /**
   * Convert the value to ISO8601 format.
   */
  protected T fromJsonISO8601(String value) {
    return convertFromInstant(parseIso(value));
  }
  


  @Override
  public void bind(DataBinder binder, T value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.TIMESTAMP);
    } else {
      binder.setTimestamp(convertToTimestamp(value), isLocal);
    }
  }

  @Override
  public T read(DataReader reader) throws SQLException {
    Timestamp ts = reader.getTimestamp(isLocal);
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
  public final T jsonRead(JsonParser parser) throws IOException {
    switch (parser.getCurrentToken()) {
      case VALUE_NUMBER_INT: {
        return convertFromMillis(parser.getLongValue());
      }
      case VALUE_NUMBER_FLOAT: {
        BigDecimal value = parser.getDecimalValue();
        long seconds = value.longValue();
        int nanoseconds = DecimalUtils.extractNanosecondDecimal(value, seconds);
        if (nanoseconds < 0) { // to support dates < 1970-01-01
          seconds --;
          nanoseconds += 1_000_000_000;
        }
        return fromJsonNanos(seconds, nanoseconds);
      }
      default: {
        return fromJsonISO8601(parser.getText());
      }
    }
  }

  @Override
  public final void jsonWrite(JsonGenerator writer, T value) throws IOException {
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
