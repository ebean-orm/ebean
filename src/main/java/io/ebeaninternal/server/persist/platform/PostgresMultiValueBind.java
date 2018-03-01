package io.ebeaninternal.server.persist.platform;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebeaninternal.server.type.ScalarType;

/**
 * Multi value binder that uses Postgres Array.
 */
public class PostgresMultiValueBind extends AbstractMultiValueBind {

  @Override
  protected String getInExpression(boolean not, ScalarType<?> type, int size, String arrayType) {
    int dbType = type.getJdbcType();
    if (dbType == ExtraDbTypes.UUID) {
      return (not) ? " != all(?::uuid[])" : " = any(?::uuid[])";
    } else {
      return (not) ? " != all(?)" : " = any(?)";
    }
  }

}
