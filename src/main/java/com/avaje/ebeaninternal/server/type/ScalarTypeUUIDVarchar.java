package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for java.util.UUID which converts to and from a VARCHAR database column.
 */
public class ScalarTypeUUIDVarchar extends ScalarTypeUUIDBase {

  protected ScalarTypeUUIDVarchar() {
    super(false, Types.VARCHAR);
  }

  @Override
  public int getLength() {
    return 40;
  }

  @Override
  public UUID toBeanType(Object value) {
    return BasicTypeConverter.toUUID(value);
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.convert(value, jdbcType);
  }

  @Override
  public void bind(DataBind b, UUID value) throws SQLException {
    if (value == null) {
      b.setNull(Types.VARCHAR);
    } else {
      b.setString(formatValue(value));
    }
  }

  @Override
  public UUID read(DataReader dataReader) throws SQLException {
    String value = dataReader.getString();
    if (value == null) {
      return null;
    } else {
      return parse(value);
    }
  }

}
