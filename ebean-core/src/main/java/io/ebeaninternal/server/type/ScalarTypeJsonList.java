package io.ebeaninternal.server.type;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.PostgresHelper;
import io.ebean.core.type.ScalarType;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareList;

import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Type which maps a List in JSON format to VARCHAR or Postgres JSON / JSONB.
 */
@SuppressWarnings("rawtypes")
class ScalarTypeJsonList extends ScalarTypeJsonCollectionValue<List> {

  /**
   * Return the appropriate ScalarType for the requested dbType and platform.
   */
  static ScalarType<?> typeFor(boolean postgres, int dbType, DocPropertyType docType, boolean nullable, boolean keepSource) {
    if (postgres) {
      switch (dbType) {
        case DbPlatformType.JSONB:
          return new ScalarTypeJsonList(DbPlatformType.JSONB, JsonStorage.postgres(PostgresHelper.JSONB_TYPE), docType, nullable, keepSource);
        case DbPlatformType.JSON:
          return new ScalarTypeJsonList(DbPlatformType.JSON, JsonStorage.postgres(PostgresHelper.JSON_TYPE), docType, nullable, keepSource);
      }
    }
    return new ScalarTypeJsonList(Types.VARCHAR, JsonStorage.VARCHAR, docType, nullable, keepSource);
  }

  ScalarTypeJsonList(int jdbcType, JsonStorage storage, DocPropertyType docType, boolean nullable, boolean keepSource) {
    super(List.class, jdbcType, storage, keepSource, nullable, docType);
  }

  @Override
  List readJson(String rawJson) {
    try {
      // parse JSON into a modifyAware list
      return EJson.parseList(rawJson, true);
    } catch (IOException e) {
      throw new TextException("Failed to parse JSON [{}] as List", rawJson, e);
    }
  }

  @Override
  public List parse(String value) {
    try {
      return EJson.parseList(value, false);
    } catch (IOException e) {
      throw new TextException("Failed to parse JSON [{}] as List", value, e);
    }
  }

  @Override
  public String formatValue(List value) {
    try {
      return EJson.write(value);
    } catch (IOException e) {
      throw new PersistenceException("Failed to format List into JSON content", e);
    }
  }

  @Override
  public List jsonRead(JsonReader parser) throws IOException {
    return EJson.parseList(parser, parser.currentToken());
  }

  @Override
  public void jsonWrite(JsonWriter writer, List value) throws IOException {
    EJson.write(value, writer);
  }

  /**
   * List mapped to VARCHAR with element conversion - used as the {@code @DbArray} fallback
   * on platforms without native array support.
   */
  static final class VarcharWithConverter extends ScalarTypeJsonList {

    private final ArrayElementConverter converter;

    VarcharWithConverter(DocPropertyType docType, boolean nullable, boolean keepSource, ArrayElementConverter converter) {
      super(Types.VARCHAR, JsonStorage.VARCHAR, docType, nullable, keepSource);
      this.converter = converter;
    }

    @Override
    List readJson(String rawJson) {
      return convert(rawJson);
    }

    @Override
    public List parse(String value) {
      return convert(value);
    }

    @SuppressWarnings("unchecked")
    private List convert(String json) {
      try {
        List<Object> rawList = EJson.parseList(json, false);
        if (rawList == null) {
          return null;
        }
        final List result = new ArrayList<>(rawList.size());
        for (Object o : rawList) {
          result.add(converter.fromSerialized(o));
        }
        return new ModifyAwareList(result);
      } catch (IOException e) {
        throw new TextException("Failed to parse JSON [{}] as List", json, e);
      }
    }
  }
}
