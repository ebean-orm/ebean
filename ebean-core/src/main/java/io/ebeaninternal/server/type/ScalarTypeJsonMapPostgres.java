package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DataBinder;

import java.sql.SQLException;

/**
 * Support for the Postgres DB types JSON and JSONB.
 */
public abstract class ScalarTypeJsonMapPostgres extends ScalarTypeJsonMap {

  final String postgresType;

  ScalarTypeJsonMapPostgres(int jdbcType, String postgresType, boolean keepSource) {
    super(jdbcType, keepSource);
    this.postgresType = postgresType;
  }

  @Override
  protected void bindNull(DataBinder binder) throws SQLException {
    binder.setObject(PostgresHelper.asObject(postgresType, null));
  }
  
  @Override
  protected void bindJson(DataBinder binder, String rawJson) throws SQLException {
    binder.setObject(PostgresHelper.asObject(postgresType, rawJson));
  }

  /**
   * ScalarType mapping java Map type to Postgres JSON database type.
   */
  public static class JSON extends ScalarTypeJsonMapPostgres {

    public JSON(boolean keepSource) {
      super(DbPlatformType.JSON, PostgresHelper.JSON_TYPE, keepSource);
    }
  }

  /**
   * ScalarType mapping java Map type to Postgres JSONB database type.
   */
  public static class JSONB extends ScalarTypeJsonMapPostgres {

    public JSONB(boolean keepSource) {
      super(DbPlatformType.JSONB, PostgresHelper.JSONB_TYPE, keepSource);
    }
  }
}
