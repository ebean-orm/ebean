package io.ebeaninternal.server.type;

import java.sql.Types;

/**
 * ScalarType for String.
 */
final class ScalarTypeLongVarchar extends ScalarTypeClob {

  public ScalarTypeLongVarchar() {
    super(true, Types.LONGVARCHAR);
  }

}
