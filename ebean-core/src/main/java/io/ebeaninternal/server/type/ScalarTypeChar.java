package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for char.
 */
public class ScalarTypeChar extends ScalarTypeBaseVarchar<Character> {

  public ScalarTypeChar() {
    super(char.class, false, Types.VARCHAR);
  }

  @Override
  public Character convertFromDbString(String dbValue) {
    return dbValue.charAt(0);
  }

  @Override
  public String convertToDbString(Character beanValue) {
    return beanValue.toString();
  }

  @Override
  public void bind(DataBinder binder, Character value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.VARCHAR);
    } else {
      String s = BasicTypeConverter.toString(value);
      binder.setString(s);
    }
  }

  @Override
  public Character read(DataReader reader) throws SQLException {
    String string = reader.getString();
    if (string == null || string.isEmpty()) {
      return null;
    } else {
      return string.charAt(0);
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toString(value);
  }

  @Override
  public Character toBeanType(Object value) {
    if (value == null) return null;
    String s = BasicTypeConverter.toString(value);
    return s.charAt(0);
  }

  @Override
  public String formatValue(Character t) {
    return t.toString();
  }

  @Override
  public Character parse(String value) {
    return value.charAt(0);
  }

}
