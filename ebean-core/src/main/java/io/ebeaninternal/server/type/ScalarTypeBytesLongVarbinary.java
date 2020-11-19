package io.ebeaninternal.server.type;

import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for Longvarbinary.
 */
public class ScalarTypeBytesLongVarbinary extends ScalarTypeBytesBase {

  public ScalarTypeBytesLongVarbinary() {
    super(true, Types.LONGVARBINARY);
  }

  @Override
  public byte[] read(DataReader reader) throws SQLException {
    return reader.getBinaryBytes();
  }
}
