package io.ebean.jackson.jsonnode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.config.DatabaseConfig;
import io.ebean.core.type.PostgresHelper;
import io.ebean.core.type.ScalarType;
import io.ebean.core.type.ScalarTypeSet;
import io.ebean.core.type.ScalarTypeSetFactory;

public class JsonNodeTypeFactory implements ScalarTypeSetFactory {

  @Override
  public ScalarTypeSet<?> createTypeSet(DatabaseConfig config, Object objectMapper) {
    if (objectMapper == null) {
      return null;
    }

    ObjectMapper mapper = (ObjectMapper) objectMapper;
    var varchar = new ScalarTypeJsonNode.Varchar(mapper);
    var clob = new ScalarTypeJsonNode.Clob(mapper);
    var blob = new ScalarTypeJsonNode.Blob(mapper);
    ScalarType<JsonNode> json = clob;  // Default for non-Postgres databases
    ScalarType<JsonNode> jsonb = clob; // Default for non-Postgres databases
    if (PostgresHelper.isPostgresCompatible(config.getDatabasePlatform())) {
      json = new ScalarTypeJsonNodePostgres.JSON(mapper);
      jsonb = new ScalarTypeJsonNodePostgres.JSONB(mapper);
    }
    return new JsonNodeSet(varchar, clob, blob, jsonb, json);
  }
}
