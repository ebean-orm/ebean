package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.text.TextException;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalTime;

/**
 * ScalarType for java.time.Duration
 */
public class ScalarTypeDuration extends ScalarTypeBase<Duration> {

  public ScalarTypeDuration() {
    super(Duration.class, false, Types.BIGINT);
  }

  protected ScalarTypeDuration(int jdbcType) {
    super(Duration.class, false, jdbcType);
  }

  @Override
  public void bind(DataBind bind, Duration value) throws SQLException {
    if (value == null) {
      bind.setNull(Types.BIGINT);
    } else {
      bind.setLong(value.getSeconds());
    }
  }

  @Override
  public Duration read(DataReader dataReader) throws SQLException {
    return Duration.ofSeconds(dataReader.getLong());
  }

  @Override
  public Duration readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return Duration.ofSeconds(dataInput.readLong());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Duration value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeLong(value.getSeconds());
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof  Long) return value;
    return ((Duration)value).getSeconds();
  }

  @Override
  public Duration toBeanType(Object value) {
    if (value instanceof Duration) return (Duration) value;
    return Duration.ofSeconds(BasicTypeConverter.toLong(value));
  }

  @Override
  public String formatValue(Duration v) {
    return v.toString();
  }

  @Override
  public Duration parse(String value) {
    return Duration.parse(value);
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public Duration convertFromMillis(long systemTimeMillis) {
    throw new TextException("Not Supported");
  }

  @Override
  public Duration jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return Duration.parse(ctx.getValueAsString());
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, Duration value) throws IOException {
    ctx.writeStringField(name, value.toString());
  }
}
