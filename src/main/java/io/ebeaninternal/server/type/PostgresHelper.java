package io.ebeaninternal.server.type;

import org.postgresql.util.PGobject;

import java.sql.SQLException;

public class PostgresHelper {

  /**
   * The Postgres JSON DB type.
   */
  public static final String JSON_TYPE = "json";

  /**
   * The Postgres JSONB DB type.
   */
  public static final String JSONB_TYPE = "jsonb";

  /**
   * Construct and return Postgres specific PG object.
   */
  public static Object asObject(String pgType, String rawJson) throws SQLException {

    PGobject pgo = new PGobject();
    pgo.setType(pgType);
    pgo.setValue(rawJson);
    return pgo;
  }
}
