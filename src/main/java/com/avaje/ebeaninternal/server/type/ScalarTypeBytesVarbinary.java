package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Types.VARBINARY to byte[].
 */
public class ScalarTypeBytesVarbinary extends ScalarTypeBytesBase {

  public ScalarTypeBytesVarbinary() {
    super(true, Types.VARBINARY);
  }

  public byte[] read(DataReader dataReader) throws SQLException {
    return dataReader.getBytes();
  }

}
