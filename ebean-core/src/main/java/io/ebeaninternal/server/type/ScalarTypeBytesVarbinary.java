package io.ebeaninternal.server.type;

import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Types.VARBINARY to byte[].
 */
public class ScalarTypeBytesVarbinary extends ScalarTypeBytesBase {

  public ScalarTypeBytesVarbinary() {
    super(true, Types.VARBINARY);
  }

  @Override
  public byte[] read(DataReader reader) throws SQLException {
    return reader.getBytes();
  }

}
