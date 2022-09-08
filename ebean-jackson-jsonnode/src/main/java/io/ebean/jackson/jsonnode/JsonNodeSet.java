package io.ebean.jackson.jsonnode;

import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.ScalarType;
import io.ebean.core.type.ScalarTypeSet;

import java.sql.Types;

class JsonNodeSet implements ScalarTypeSet<JsonNode> {

  final ScalarType<JsonNode> varchar;
  final ScalarType<JsonNode> clob;
  final ScalarType<JsonNode> blob;
  final ScalarType<JsonNode> jsonb;
  final ScalarType<JsonNode> json;

  JsonNodeSet(ScalarType<JsonNode> varchar, ScalarType<JsonNode> clob, ScalarType<JsonNode> blob, ScalarType<JsonNode> jsonb, ScalarType<JsonNode> json) {
    this.varchar = varchar;
    this.clob = clob;
    this.blob = blob;
    this.jsonb = jsonb;
    this.json = json;
  }

  @Override
  public Class<?> type() {
    return JsonNode.class;
  }

  @Override
  public ScalarType<?> defaultType() {
    return json;
  }

  @Override
  public ScalarType<JsonNode> forType(int dbType) {
    switch (dbType) {
      case Types.VARCHAR:
        return varchar;
      case Types.BLOB:
        return blob;
      case Types.CLOB:
        return clob;
      case DbPlatformType.JSONB:
        return jsonb;
      default:
        return json;
    }
  }
}
