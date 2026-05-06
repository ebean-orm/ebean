package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.PostgresHelper;
import io.ebean.core.type.ScalarType;

import java.sql.SQLException;

/**
 * Support for the Postgres DB types JSON and JSONB for Map<Enum, Object>.
 */
abstract class ScalarTypeJsonMapEnumPostgres<T extends Enum<T>> extends ScalarTypeJsonMapEnum<T> {

  private final String postgresType;

  ScalarTypeJsonMapEnumPostgres(int jdbcType, String postgresType, ScalarType<T> enumScalarType, boolean keepSource) {
    super(jdbcType, enumScalarType, keepSource);
    this.postgresType = postgresType;
  }

  @Override
  protected final void bindNull(DataBinder binder) throws SQLException {
    binder.setObject(PostgresHelper.asObject(postgresType, null));
  }

  @Override
  protected final void bindJson(DataBinder binder, String rawJson) throws SQLException {
    binder.setObject(PostgresHelper.asObject(postgresType, rawJson));
  }

  /**
   * ScalarType mapping java Map<Enum, Object> type to Postgres JSON database type.
   */
  static final class JSON<T extends Enum<T>> extends ScalarTypeJsonMapEnumPostgres<T> {

    JSON(ScalarType<T> enumScalarType, boolean keepSource) {
      super(DbPlatformType.JSON, PostgresHelper.JSON_TYPE, enumScalarType, keepSource);
    }
  }

  /**
   * ScalarType mapping java Map<Enum, Object> type to Postgres JSONB database type.
   */
  static final class JSONB<T extends Enum<T>> extends ScalarTypeJsonMapEnumPostgres<T> {

    JSONB(ScalarType<T> enumScalarType, boolean keepSource) {
      super(DbPlatformType.JSONB, PostgresHelper.JSONB_TYPE, enumScalarType, keepSource);
    }
  }
}
