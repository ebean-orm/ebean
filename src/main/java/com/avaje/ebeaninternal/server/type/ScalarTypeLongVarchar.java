package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for String.
 */
public class ScalarTypeLongVarchar extends ScalarTypeClob {

  public ScalarTypeLongVarchar() {
    super(true, Types.LONGVARCHAR);
  }

  @Override
  public String read(DataReader dataReader) throws SQLException {
    return dataReader.getStringFromStream();
  }
}
