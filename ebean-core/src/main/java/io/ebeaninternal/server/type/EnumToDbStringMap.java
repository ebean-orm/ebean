package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.sql.Types;

/**
 * Used to map Enum values to database string/varchar values.
 */
public class EnumToDbStringMap extends EnumToDbValueMap<String> {

  @Override
  public int getDbType() {
    return Types.VARCHAR;
  }

  @Override
  public EnumToDbStringMap add(Object beanValue, String dbValue, String name) {
    addInternal(beanValue, dbValue, name);
    return this;
  }

  @Override
  public void bind(DataBinder binder, Object value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.VARCHAR);
    } else {
      String s = getDbValue(value);
      binder.setString(s);
    }
  }

  @Override
  public Object read(DataReader reader) throws SQLException {
    String s = reader.getString();
    if (s == null) {
      return null;
    } else {
      return getBeanValue(s);
    }
  }

}
