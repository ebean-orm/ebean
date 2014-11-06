package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for String.
 */
public class ScalarTypeClob extends ScalarTypeBaseVarchar<String> {

  static final int clobBufferSize = 512;

  static final int stringInitialSize = 512;

  protected ScalarTypeClob(boolean jdbcNative, int jdbcType) {
    super(String.class, jdbcNative, jdbcType);
  }

  public ScalarTypeClob() {
    super(String.class, true, Types.CLOB);
  }

  @Override
  public String convertFromDbString(String dbValue) {
    return dbValue;
  }

  @Override
  public String convertToDbString(String beanValue) {
    return beanValue;
  }

  public void bind(DataBind b, String value) throws SQLException {
    if (value == null) {
      b.setNull(Types.VARCHAR);
    } else {
      b.setString(value);
    }
  }

  public String read(DataReader dataReader) throws SQLException {

    return dataReader.getStringClob();
  }

  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toString(value);
  }

  public String toBeanType(Object value) {
    return BasicTypeConverter.toString(value);
  }

  public String formatValue(String t) {
    return t;
  }

  public String parse(String value) {
    return value;
  }

}
