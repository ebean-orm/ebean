package io.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;

import io.ebean.type.DataReader;

/**
 * ScalarType for Types.VARBINARY to byte[].
 */
public class ScalarTypeBytesVarbinary extends ScalarTypeBytesBase {

  public ScalarTypeBytesVarbinary() {
    super(true, Types.VARBINARY);
  }

  @Override
  public byte[] read(DataReader dataReader) throws SQLException {
    return dataReader.getBytes();
  }

}
