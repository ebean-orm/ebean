package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

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

  public void bind(DataBind b, Character value) throws SQLException {
    if (value == null) {
      b.setNull(Types.VARCHAR);
    } else {
      String s = BasicTypeConverter.toString(value);
      b.setString(s);
    }
  }

  public Character read(DataReader dataReader) throws SQLException {
    String string = dataReader.getString();
    if (string == null || string.length() == 0) {
      return null;
    } else {
      return string.charAt(0);
    }
  }

  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toString(value);
  }

  public Character toBeanType(Object value) {
    String s = BasicTypeConverter.toString(value);
    return s.charAt(0);
  }

  public String formatValue(Character t) {
    return t.toString();
  }

  public Character parse(String value) {
    return value.charAt(0);
  }

}
