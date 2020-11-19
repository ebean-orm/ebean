package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Long and long.
 */
public class ScalarTypeLong extends ScalarTypeBase<Long> {

  public ScalarTypeLong() {
    super(Long.class, true, Types.BIGINT);
  }

  @Override
  public void bind(DataBinder binder, Long value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.BIGINT);
    } else {
      binder.setLong(value);
    }
  }

  @Override
  public Long read(DataReader reader) throws SQLException {
    return reader.getLong();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toLong(value);
  }

  @Override
  public Long toBeanType(Object value) {
    return BasicTypeConverter.toLong(value);
  }

  @Override
  public String formatValue(Long t) {
    return t.toString();
  }

  @Override
  public Long parse(String value) {
    return Long.valueOf(value);
  }

  @Override
  public long asVersion(Long value) {
    return value;
  }

  @Override
  public Long convertFromMillis(long systemTimeMillis) {
    return systemTimeMillis;
  }

  @Override
  public boolean isDateTimeCapable() {
    return true;
  }

  @Override
  public Long readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return dataInput.readLong();
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Long value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeLong(value);
    }
  }

  @Override
  public Long jsonRead(JsonParser parser) throws IOException {
    return parser.getLongValue();
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Long value) throws IOException {
    writer.writeNumber(value);
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.LONG;
  }

}
