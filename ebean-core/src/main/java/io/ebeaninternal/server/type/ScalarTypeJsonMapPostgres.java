package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.PostgresHelper;

import java.sql.SQLException;

/**
 * Support for the Postgres DB types JSON and JSONB.
 */
abstract class ScalarTypeJsonMapPostgres extends ScalarTypeJsonMap {

  private final String postgresType;

  ScalarTypeJsonMapPostgres(int jdbcType, String postgresType, boolean keepSource) {
    super(jdbcType, keepSource);
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
   * ScalarType mapping java Map type to Postgres JSON database type.
   */
  static final class JSON extends ScalarTypeJsonMapPostgres {

    JSON(boolean keepSource) {
      super(DbPlatformType.JSON, PostgresHelper.JSON_TYPE, keepSource);
    }
  }

  /**
   * ScalarType mapping java Map type to Postgres JSONB database type.
   */
  static final class JSONB extends ScalarTypeJsonMapPostgres {

    JSONB(boolean keepSource) {
      super(DbPlatformType.JSONB, PostgresHelper.JSONB_TYPE, keepSource);
    }
  }
}
