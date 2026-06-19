package io.ebeaninternal.server.type;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.PostgresHelper;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;

import java.io.IOException;
import java.sql.Types;
import java.util.Map;

/**
 * Type which maps {@code Map<String,Object>} to JSON stored in VARCHAR, CLOB, BLOB,
 * or Postgres JSON / JSONB.
 */
@SuppressWarnings("rawtypes")
final class ScalarTypeJsonMap extends ScalarTypeJsonValue<Map> {

  /**
   * Return the ScalarType for the requested dbType and platform.
   */
  static ScalarTypeJsonMap typeFor(boolean postgres, int dbType, boolean keepSource) {
    switch (dbType) {
      case Types.VARCHAR:
        return new ScalarTypeJsonMap(Types.VARCHAR, JsonStorage.VARCHAR, keepSource);
      case Types.BLOB:
        return new ScalarTypeJsonMap(Types.BLOB, JsonStorage.BLOB, keepSource);
      case Types.CLOB:
        return new ScalarTypeJsonMap(Types.CLOB, JsonStorage.CLOB, keepSource);
      case DbPlatformType.JSONB:
        return postgres
          ? new ScalarTypeJsonMap(DbPlatformType.JSONB, JsonStorage.postgres(PostgresHelper.JSONB_TYPE), keepSource)
          : new ScalarTypeJsonMap(Types.CLOB, JsonStorage.CLOB, keepSource);
      case DbPlatformType.JSON:
        return postgres
          ? new ScalarTypeJsonMap(DbPlatformType.JSON, JsonStorage.postgres(PostgresHelper.JSON_TYPE), keepSource)
          : new ScalarTypeJsonMap(Types.CLOB, JsonStorage.CLOB, keepSource);
      default:
        throw new IllegalStateException("Unknown dbType " + dbType);
    }
  }

  private ScalarTypeJsonMap(int jdbcType, JsonStorage storage, boolean keepSource) {
    super(Map.class, jdbcType, storage, keepSource, true, null, DocPropertyType.OBJECT);
  }

  @Override
  Map readJson(String rawJson) {
    return parse(rawJson);
  }

  @Override
  public Map parse(String value) {
    try {
      // return a modify aware map
      return EJson.parseObject(value, true);
    } catch (IOException e) {
      throw new TextException("Failed to parse JSON [{}] as Object", value, e);
    }
  }

  @Override
  public String formatValue(Map value) {
    try {
      return EJson.write(value);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  @Override
  public Map jsonRead(JsonReader parser) throws IOException {
    return EJson.parseObject(parser, parser.currentToken());
  }

  @Override
  public void jsonWrite(JsonWriter writer, Map value) throws IOException {
    EJson.write(value, writer);
  }
}
