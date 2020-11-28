package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Postgres Hstore type which maps Map<String,String> to a single 'HStore column' in the DB.
 */
public class ScalarTypeUUIDNative extends ScalarTypeUUIDBase {

  public ScalarTypeUUIDNative() {
    super(false, DbPlatformType.UUID);
  }

  @Override
  public UUID read(DataReader reader) throws SQLException {
    Object value = reader.getObject();
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      return UUID.fromString((String) value);
    }
    return (UUID) value;
  }

  @Override
  public void bind(DataBinder binder, UUID value) throws SQLException {
    binder.setObject(value);
  }

  @Override
  public Object toJdbcType(Object value) {
    return value;
  }

}
