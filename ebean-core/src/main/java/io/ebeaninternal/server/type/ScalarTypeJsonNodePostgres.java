package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.DbPlatformType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;

/**
 * Support for mapping JsonNode to Postgres DB types JSON and JSONB.
 */
public abstract class ScalarTypeJsonNodePostgres extends ScalarTypeJsonNode {

  final ObjectMapper objectMapper;

  final String postgresType;

  ScalarTypeJsonNodePostgres(ObjectMapper objectMapper, int jdbcType, String postgresType) {
    super(objectMapper, jdbcType);
    this.objectMapper = objectMapper;
    this.postgresType = postgresType;
  }

  @Override
  public void bind(DataBind bind, JsonNode value) throws SQLException {
    String rawJson = (value == null) ? null : formatValue(value);
    bind.setObject(PostgresHelper.asObject(postgresType, rawJson));
  }

  /**
   * ScalarType mapping JsonNode to Postgres JSON database type.
   */
  public static class JSON extends ScalarTypeJsonNodePostgres {

    public JSON(ObjectMapper objectMapper) {
      super(objectMapper, DbPlatformType.JSON, PostgresHelper.JSON_TYPE);
    }
  }

  /**
   * ScalarType mapping JsonNode to Postgres JSONB database type.
   */
  public static class JSONB extends ScalarTypeJsonNodePostgres {

    public JSONB(ObjectMapper objectMapper) {
      super(objectMapper, DbPlatformType.JSONB, PostgresHelper.JSONB_TYPE);
    }
  }
}
