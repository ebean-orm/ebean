package io.ebeaninternal.server.type;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.ebean.core.type.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;

/**
 * ScalarType for java.time.Duration (with seconds precision).
 */
class ScalarTypeDuration extends ScalarTypeBase<Duration> {

  ScalarTypeDuration() {
    super(Duration.class, false, Types.BIGINT);
  }

  ScalarTypeDuration(int jdbcType) {
    super(Duration.class, false, jdbcType);
  }

  BigDecimal convertToBigDecimal(Duration value) {
    return (value == null) ? null : ScalarTypeUtils.toDecimal(value);
  }

  Duration convertFromBigDecimal(BigDecimal value) {
    return (value == null) ? null : ScalarTypeUtils.toDuration(value);
  }

  @Override
  public void bind(DataBinder binder, Duration value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.BIGINT);
    } else {
      binder.setLong(value.getSeconds());
    }
  }

  @Override
  public Duration read(DataReader reader) throws SQLException {
    Long value = reader.getLong();
    return (value == null) ? null : Duration.ofSeconds(value);
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Long) return value;
    return ((Duration) value).getSeconds();
  }

  @Override
  public Duration toBeanType(Object value) {
    if (value instanceof Duration) return (Duration) value;
    if (value == null) return null;
    return Duration.ofSeconds(BasicTypeConverter.toLong(value));
  }

  @Override
  public Duration readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return convertFromBigDecimal(new BigDecimal(dataInput.readUTF()));
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Duration value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      ScalarHelp.writeUTF(dataOutput, convertToBigDecimal(value).toString());
    }
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
  public Duration jsonRead(JsonReader parser) throws IOException {
    return Duration.parse(parser.readString());
  }

  @Override
  public void jsonWrite(JsonWriter writer, Duration value) throws IOException {
    writer.value(value.toString());
  }

  @Override
  public DocPropertyType docType() {
    return DocPropertyType.KEYWORD;
  }

}
