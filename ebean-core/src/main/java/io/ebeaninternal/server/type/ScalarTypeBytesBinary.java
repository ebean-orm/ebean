package io.ebeaninternal.server.type;

import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Types.BINARY to byte[].
 */
final class ScalarTypeBytesBinary extends ScalarTypeBytesBase {

  ScalarTypeBytesBinary() {
    super(true, Types.BINARY);
  }

  @Override
  public byte[] read(DataReader reader) throws SQLException {
    return reader.getBytes();
  }

}
