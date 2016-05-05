package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.dbplatform.DbType;

import java.sql.SQLException;
import java.util.Map;

/**
 * Support for the Postgres DB types JSON and JSONB.
 */
public abstract class ScalarTypeJsonMapPostgres extends ScalarTypeJsonMap {

  final String postgresType;

  ScalarTypeJsonMapPostgres(int jdbcType, String postgresType) {
    super(jdbcType);
    this.postgresType = postgresType;
  }

  @Override
  public void bind(DataBind bind, Map value) throws SQLException {

    String rawJson = (value == null) ? null : formatValue(value);
    bind.setObject(PostgresHelper.asObject(postgresType, rawJson));
  }

  /**
   * ScalarType mapping java Map type to Postgres JSON database type.
   */
  public static class JSON extends ScalarTypeJsonMapPostgres {

    public JSON() {
      super(DbType.JSON, PostgresHelper.JSON_TYPE);
    }
  }

  /**
   * ScalarType mapping java Map type to Postgres JSONB database type.
   */
  public static class JSONB extends ScalarTypeJsonMapPostgres {

    public JSONB() {
      super(DbType.JSONB, PostgresHelper.JSONB_TYPE);
    }
  }
}
