package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * ScalarType for java.util.UUID which converts to and from a VARCHAR database column.
 */
public class ScalarTypeUUIDVarchar extends ScalarTypeUUIDBase {

  protected ScalarTypeUUIDVarchar() {
    super(false, Types.VARCHAR);
  }

  @Override
  public int getLength() {
    return 40;
  }

  @Override
  public void bind(DataBinder binder, UUID value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.VARCHAR);
    } else {
      binder.setString(formatValue(value));
    }
  }

  @Override
  public UUID read(DataReader reader) throws SQLException {
    String value = reader.getString();
    if (value == null) {
      return null;
    } else {
      return parse(value);
    }
  }

}
