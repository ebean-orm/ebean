package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.config.dbplatform.ExtraDbTypes;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.PostgresHelper;
import io.ebean.core.type.ScalarType;

import java.sql.SQLException;

final class ScalarTypeJsonString {

  static final Postgres JSONB = new Postgres(ExtraDbTypes.JSONB, PostgresHelper.JSONB_TYPE);
  static final Postgres JSON = new Postgres(ExtraDbTypes.JSON, PostgresHelper.JSON_TYPE);

  static ScalarType<?> typeFor(boolean postgres, int dbType) {
    if (postgres) {
      switch (dbType) {
        case DbPlatformType.JSONB:
          return JSONB;
        case DbPlatformType.JSON:
          return JSON;
      }
    }
    return ScalarTypeString.INSTANCE;
  }

  private static class Postgres extends ScalarTypeStringBase {
    final String postgresType;

    Postgres(int jdbcType, String postgresType) {
      super(true, jdbcType);
      this.postgresType = postgresType;
    }

    @Override
    public void bind(DataBinder binder, String rawJson) throws SQLException {
      binder.setObject(PostgresHelper.asObject(postgresType, rawJson));
    }
  }

}
