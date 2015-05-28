package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.dbplatform.DbType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

/**
 * Support for mapping JsonNode to Postgres DB types JSON and JSONB.
 */
public abstract class ScalarTypeJsonNodePostgres extends ScalarTypeJsonNode {

  private static final String POSTGRES_TYPE_JSON = "json";

  private static final String POSTGRES_TYPE_JSONB = "jsonb";

  final ObjectMapper objectMapper;

  final String postgresType;

  ScalarTypeJsonNodePostgres(ObjectMapper objectMapper, int jdbcType, String postgresType) {
    super(objectMapper, jdbcType);
    this.objectMapper = objectMapper;
    this.postgresType = postgresType;
  }

  @Override
  public void bind(DataBind dataBind, JsonNode value) throws SQLException {

    String rawJson = (value == null) ? null : formatValue(value);

    PGobject pgo = new PGobject();
    pgo.setType(postgresType);
    pgo.setValue(rawJson);
    dataBind.setObject(pgo);
  }

  /**
   * ScalarType mapping JsonNode to Postgres JSON database type.
   */
  public static class JSON extends ScalarTypeJsonNodePostgres {

    public JSON(ObjectMapper objectMapper) {
      super(objectMapper, DbType.JSON, POSTGRES_TYPE_JSON);
    }
  }

  /**
   * ScalarType mapping JsonNode to Postgres JSONB database type.
   */
  public static class JSONB extends ScalarTypeJsonNodePostgres {

    public JSONB(ObjectMapper objectMapper) {
      super(objectMapper, DbType.JSONB, POSTGRES_TYPE_JSONB);
    }
  }
}
