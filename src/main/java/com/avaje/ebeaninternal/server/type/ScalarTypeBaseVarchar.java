package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebean.text.TextException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Base ScalarType for types which converts to and from a VARCHAR database
 * column.
 */
public abstract class ScalarTypeBaseVarchar<T> extends ScalarTypeBase<T> {

  public ScalarTypeBaseVarchar(Class<T> type) {
    super(type, false, Types.VARCHAR);
  }

  public ScalarTypeBaseVarchar(Class<T> type, boolean jdbcNative, int jdbcType) {
    super(type, jdbcNative, jdbcType);
  }

  /**
   * Format the target type to a string.
   */
  public abstract String formatValue(T v);

  /**
   * Parse from a formatted string value.
   */
  public abstract T parse(String value);

  /**
   * Convert from DB string value to the target type.
   */
  public abstract T convertFromDbString(String dbValue);

  /**
   * Convert to DB string from the target type.
   */
  public abstract String convertToDbString(T beanValue);

  @Override
  public void bind(DataBind b, T value) throws SQLException {
    if (value == null) {
      b.setNull(Types.VARCHAR);

    } else {
      b.setString(convertToDbString(value));
    }
  }

  @Override
  public T read(DataReader dataReader) throws SQLException {
    String s = dataReader.getString();
    if (s == null) {
      return null;
    } else {
      return convertFromDbString(s);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public T toBeanType(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      return parse((String) value);
    }
    return (T) value;
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof String) {
      return parse((String) value);
    }
    return value;
  }

  @Override
  public T convertFromMillis(long systemTimeMillis) {
    throw new TextException("Not Supported");
  }
  
  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public String format(Object v) {
    return formatValue((T) v);
  }

  @Override
  public T readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      String val = dataInput.readUTF();
      return convertFromDbString(val);
    }
  }

  public void writeData(DataOutput dataOutput, T value) throws IOException {

    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      String s = convertToDbString(value);
      dataOutput.writeUTF(s);
    }
  }
  
  @Override
  public T jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return parse(ctx.getValueAsString());
  }
  
  @Override
  public void jsonWrite(JsonGenerator ctx, String name, T value) throws IOException {
    ctx.writeStringField(name, format(value));
  }
}
