package io.ebeaninternal.server.type;

import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for BLOB.
 */
final class ScalarTypeBytesBlob extends ScalarTypeBytesBase {

  ScalarTypeBytesBlob() {
    super(true, Types.BLOB);
  }

  @Override
  public byte[] read(DataReader reader) throws SQLException {
    return reader.getBinaryBytes();
  }

}
