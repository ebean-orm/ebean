package io.ebeaninternal.server.type;

import java.sql.Types;

/**
 * ScalarType for String.
 */
public class ScalarTypeString extends ScalarTypeStringBase {

  public static final ScalarTypeString INSTANCE = new ScalarTypeString();

  private ScalarTypeString() {
    super(true, Types.VARCHAR);
  }
}
