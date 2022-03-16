package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for String.
 */
class ScalarTypeClob extends ScalarTypeStringBase {

  ScalarTypeClob(boolean jdbcNative, int jdbcType) {
    super(jdbcNative, jdbcType);
  }

  ScalarTypeClob() {
    super(true, Types.CLOB);
  }

  @Override
  public void bind(DataBinder binder, String value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.VARCHAR);
    } else {
      binder.setClob(value);
    }
  }

  @Override
  public String read(DataReader reader) throws SQLException {
    return reader.getStringFromStream();
  }

}
