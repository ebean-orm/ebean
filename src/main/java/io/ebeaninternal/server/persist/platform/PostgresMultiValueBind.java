package io.ebeaninternal.server.persist.platform;

import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebeaninternal.server.type.ScalarType;

/**
 * Multi value binder that uses Postgres Array and unnest.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class PostgresMultiValueBind extends AbstractMultiValueBind {

  @Override
  public String getInExpression(ScalarType<?> type, int size) {
    int dbType = type.getJdbcType();
    if (dbType == ExtraDbTypes.UUID) {
      return " in (select(unnest(?))::uuid) ";
    }
    String arrayType = getArrayType(dbType);
    if (arrayType == null) {
      return super.getInExpression(type, size);
    } else {
      return " in (select(unnest(?))) ";
    }
  }

  @Override
  protected String getArrayType(int dbType) {
    if (dbType == ExtraDbTypes.UUID) {
      return "uuid";
    }
    return super.getArrayType(dbType);
  }
}
