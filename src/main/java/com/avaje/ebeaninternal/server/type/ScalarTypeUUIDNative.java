package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.dbplatform.DbType;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Postgres Hstore type which maps Map<String,String> to a single 'HStore column' in the DB.
 */
public class ScalarTypeUUIDNative extends ScalarTypeUUIDBase {

  public ScalarTypeUUIDNative() {
    super(false, DbType.UUID);
  }

  @Override
  public UUID read(DataReader dataReader) throws SQLException {
    Object value = dataReader.getObject();
    if (value == null) {
      return null;
    }
    return (UUID) value;
  }

  @Override
  public void bind(DataBind b, UUID value) throws SQLException {
    b.setObject(value);
  }

  @Override
  public Object toJdbcType(Object value) {
    return value;
  }

  @Override
  public UUID toBeanType(Object value) {
    return (UUID) value;
  }

}
