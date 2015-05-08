package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Longvarbinary.
 */
public class ScalarTypeBytesLongVarbinary extends ScalarTypeBytesBase {

  public ScalarTypeBytesLongVarbinary() {
    super(true, Types.LONGVARBINARY);
  }

  public byte[] read(DataReader dataReader) throws SQLException {
    return dataReader.getBinaryBytes();
  }
}
