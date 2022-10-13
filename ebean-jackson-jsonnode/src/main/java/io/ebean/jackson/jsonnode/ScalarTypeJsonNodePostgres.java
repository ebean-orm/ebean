package io.ebean.jackson.jsonnode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.PostgresHelper;

import java.sql.SQLException;

/**
 * Support for mapping JsonNode to Postgres DB types JSON and JSONB.
 */
abstract class ScalarTypeJsonNodePostgres extends ScalarTypeJsonNode {

  final ObjectMapper objectMapper;
  final String postgresType;

  ScalarTypeJsonNodePostgres(ObjectMapper objectMapper, int jdbcType, String postgresType) {
    super(objectMapper, jdbcType);
    this.objectMapper = objectMapper;
    this.postgresType = postgresType;
  }

  @Override
  public void bind(DataBinder binder, JsonNode value) throws SQLException {
    String rawJson = (value == null) ? null : formatValue(value);
    binder.setObject(PostgresHelper.asObject(postgresType, rawJson));
  }

  /**
   * ScalarType mapping JsonNode to Postgres JSON database type.
   */
  static final class JSON extends ScalarTypeJsonNodePostgres {

    public JSON(ObjectMapper objectMapper) {
      super(objectMapper, DbPlatformType.JSON, PostgresHelper.JSON_TYPE);
    }
  }

  /**
   * ScalarType mapping JsonNode to Postgres JSONB database type.
   */
  static final class JSONB extends ScalarTypeJsonNodePostgres {

    public JSONB(ObjectMapper objectMapper) {
      super(objectMapper, DbPlatformType.JSONB, PostgresHelper.JSONB_TYPE);
    }
  }
}
