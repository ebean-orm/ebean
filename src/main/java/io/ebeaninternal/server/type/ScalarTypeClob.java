package io.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for String.
 */
public class ScalarTypeClob extends ScalarTypeStringBase {

  ScalarTypeClob(boolean jdbcNative, int jdbcType) {
    super(jdbcNative, jdbcType);
  }

  public ScalarTypeClob() {
    super(true, Types.CLOB);
  }

  @Override
  public String read(DataReader dataReader) throws SQLException {
    return dataReader.getStringFromStream();
  }

}
