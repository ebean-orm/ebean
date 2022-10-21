package io.ebeaninternal.server.persist.platform;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebean.core.type.ScalarType;

/**
 * Multi value binder that uses Postgres Array.
 */
public final class PostgresMultiValueBind extends AbstractMultiValueBind {

  @Override
  public String getInExpression(boolean not, ScalarType<?> type, int size) {
    int dbType = type.jdbcType();
    if (dbType == ExtraDbTypes.UUID) {
      return (not) ? " != all(?::uuid[])" : " = any(?::uuid[])";
    }
    if (dbType == ExtraDbTypes.INET) {
      return (not) ? " != all(?::inet[])" : " = any(?::inet[])";
    }
    String arrayType = getArrayType(dbType);
    if (arrayType == null) {
      return super.getInExpression(not, type, size);
    } else {
      return (not) ? " != all(?)" : " = any(?)";
    }
  }

}
