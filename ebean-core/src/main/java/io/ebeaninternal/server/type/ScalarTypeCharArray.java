package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for char[].
 */
final class ScalarTypeCharArray extends ScalarTypeBaseVarchar<char[]> {

  ScalarTypeCharArray() {
    super(char[].class, false, Types.VARCHAR);
  }

  @Override
  public char[] convertFromDbString(String dbValue) {
    return dbValue.toCharArray();
  }

  @Override
  public String convertToDbString(char[] beanValue) {
    return new String(beanValue);
  }

  @Override
  public void bind(DataBinder binder, char[] value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.VARCHAR);
    } else {
      String s = BasicTypeConverter.toString(value);
      binder.setString(s);
    }
  }

  @Override
  public char[] read(DataReader reader) throws SQLException {
    String string = reader.getString();
    if (string == null) {
      return null;
    } else {
      return string.toCharArray();
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toString(value);
  }

  @Override
  public char[] toBeanType(Object value) {
    if (value == null) return null;
    String s = BasicTypeConverter.toString(value);
    return s.toCharArray();
  }

  @Override
  public String formatValue(char[] t) {
    return String.valueOf(t);
  }

  @Override
  public char[] parse(String value) {
    return value.toCharArray();
  }

  @Override
  public char[] jsonRead(JsonParser parser) throws IOException {
    return parser.getValueAsString().toCharArray();
  }

  public void jsonWrite(JsonGenerator ctx, String name, char[] value) throws IOException {
    ctx.writeStringField(name, String.valueOf(value));
  }
}
