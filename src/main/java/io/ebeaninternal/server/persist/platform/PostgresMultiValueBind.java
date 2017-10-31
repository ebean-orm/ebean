package io.ebeaninternal.server.persist.platform;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebeaninternal.server.type.ScalarType;

/**
 * Multi value binder that uses Postgres Array.
 */
public class PostgresMultiValueBind extends AbstractMultiValueBind {

  @Override
  public String getInExpression(boolean not, ScalarType<?> type, int size) {
    int dbType = type.getJdbcType();
    if (dbType == ExtraDbTypes.UUID) {
      return (not) ? " != all(?::uuid[])" : " = any(?::uuid[])";
    }
    String arrayType = getArrayType(dbType);
    if (arrayType == null) {
      return super.getInExpression(not, type, size);
    } else {
      return (not) ? " != all(?)" : " = any(?)";
    }
  }

}
