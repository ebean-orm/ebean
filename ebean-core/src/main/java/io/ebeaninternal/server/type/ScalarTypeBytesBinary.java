package io.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Types.BINARY to byte[].
 */
public class ScalarTypeBytesBinary extends ScalarTypeBytesBase {

  public ScalarTypeBytesBinary() {
    super(true, Types.BINARY);
  }

  @Override
  public byte[] read(DataReader reader) throws SQLException {
    return reader.getBytes();
  }

}
