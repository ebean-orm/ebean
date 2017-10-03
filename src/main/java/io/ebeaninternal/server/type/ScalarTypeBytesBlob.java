package io.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;

import io.ebean.databind.DataReader;

/**
 * ScalarType for BLOB.
 */
public class ScalarTypeBytesBlob extends ScalarTypeBytesBase {

  public ScalarTypeBytesBlob() {
    super(true, Types.BLOB);
  }

  @Override
  public byte[] read(DataReader dataReader) throws SQLException {

    return dataReader.getBinaryBytes();
  }

}
