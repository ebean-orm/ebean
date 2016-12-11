package io.ebeaninternal.server.type;

import io.ebeaninternal.server.core.BasicTypeConverter;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for String.
 */
public class ScalarTypeClob extends ScalarTypeBaseVarchar<String> {

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

  @Override
  public void bind(DataBind b, String value) throws SQLException {
    if (value == null) {
      b.setNull(Types.VARCHAR);
    } else {
      b.setString(value);
    }
  }

  @Override
  public String read(DataReader dataReader) throws SQLException {

    return dataReader.getStringFromStream();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toString(value);
  }

  @Override
  public String toBeanType(Object value) {
    return BasicTypeConverter.toString(value);
  }

  @Override
  public String formatValue(String t) {
    return t;
  }

  @Override
  public String parse(String value) {
    return value;
  }

}
