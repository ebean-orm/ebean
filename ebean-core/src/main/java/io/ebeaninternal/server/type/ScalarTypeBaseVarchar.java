package io.ebeaninternal.server.type;

import io.ebean.text.TextException;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

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
  @Override
  public abstract String formatValue(T v);

  /**
   * Parse from a formatted string value.
   */
  @Override
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
      return value;
    }
    return format(value);
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
  public String format(Object value) {
    return formatValue((T) value);
  }

  @Override
  public T readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return convertFromDbString(dataInput.readUTF());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, T value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      ScalarHelp.writeUTF(dataOutput, convertToDbString(value));
    }
  }

  @Override
  public T jsonRead(JsonParser parser) throws IOException {
    return parse(parser.getValueAsString());
  }

  @Override
  public void jsonWrite(JsonGenerator writer, T value) throws IOException {
    writer.writeString(format(value));
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.TEXT;
  }

}
