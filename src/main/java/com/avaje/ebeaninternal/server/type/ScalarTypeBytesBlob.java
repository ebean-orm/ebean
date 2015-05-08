package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for BLOB.
 */
public class ScalarTypeBytesBlob extends ScalarTypeBytesBase {

  public ScalarTypeBytesBlob() {
    super(true, Types.BLOB);
  }

  public byte[] read(DataReader dataReader) throws SQLException {

    return dataReader.getBlobBytes();
  }

}
