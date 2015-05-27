package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.dbplatform.DbType;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.Map;

/**
 * Support for the Postgres DB types JSON and JSONB.
 */
public abstract class ScalarTypeJsonMapPostgres extends ScalarTypeJsonMap {

  private static final String POSTGRES_TYPE_JSON = "json";

  private static final String POSTGRES_TYPE_JSONB = "jsonb";

  final String postgresType;

  ScalarTypeJsonMapPostgres(int jdbcType, String postgresType) {
    super(jdbcType);
    this.postgresType = postgresType;
  }

  @Override
  public void bind(DataBind b, Map value) throws SQLException {

    String rawJson = (value == null) ? null : formatValue(value);

    PGobject pgo = new PGobject();
    pgo.setType(postgresType);
    pgo.setValue(rawJson);
    b.setObject(pgo);
  }

  /**
   * ScalarType mapping java Map type to Postgres JSON database type.
   */
  public static class JSON extends ScalarTypeJsonMapPostgres {

    public JSON() {
      super(DbType.JSON, POSTGRES_TYPE_JSON);
    }
  }

  /**
   * ScalarType mapping java Map type to Postgres JSONB database type.
   */
  public static class JSONB extends ScalarTypeJsonMapPostgres {

    public JSONB() {
      super(DbType.JSONB, POSTGRES_TYPE_JSONB);
    }
  }
}
