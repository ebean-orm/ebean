package io.ebeaninternal.server.type;

import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for BLOB.
 */
public class ScalarTypeBytesBlob extends ScalarTypeBytesBase {

  public ScalarTypeBytesBlob() {
    super(true, Types.BLOB);
  }

  @Override
  public byte[] read(DataReader reader) throws SQLException {
    return reader.getBinaryBytes();
  }

}
