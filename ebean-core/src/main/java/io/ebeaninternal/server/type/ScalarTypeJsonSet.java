package io.ebeaninternal.server.type;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.ebean.annotation.MutationDetection;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.PostgresHelper;
import io.ebean.core.type.ScalarType;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareSet;

import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Type which maps a Set in JSON format to VARCHAR or Postgres JSON / JSONB.
 */
@SuppressWarnings("rawtypes")
class ScalarTypeJsonSet extends ScalarTypeJsonCollectionValue<Set> {

  /**
   * Return the appropriate ScalarType for the requested dbType and platform.
   */
  static ScalarType<?> typeFor(boolean postgres, int dbType, DocPropertyType docType, boolean nullable, MutationDetection mutationDetection) {
    if (postgres) {
      switch (dbType) {
        case DbPlatformType.JSONB:
          return new ScalarTypeJsonSet(DbPlatformType.JSONB, JsonStorage.postgres(PostgresHelper.JSONB_TYPE), docType, nullable, mutationDetection);
        case DbPlatformType.JSON:
          return new ScalarTypeJsonSet(DbPlatformType.JSON, JsonStorage.postgres(PostgresHelper.JSON_TYPE), docType, nullable, mutationDetection);
      }
    }
    return new ScalarTypeJsonSet(Types.VARCHAR, JsonStorage.VARCHAR, docType, nullable, mutationDetection);
  }

  ScalarTypeJsonSet(int jdbcType, JsonStorage storage, DocPropertyType docType, boolean nullable, MutationDetection mutationDetection) {
    super(Set.class, jdbcType, storage, mutationDetection, nullable, docType);
  }

  @Override
  Set readJson(String rawJson) {
    try {
      // parse JSON into a modifyAware set
      return EJson.parseSet(rawJson, true);
    } catch (IOException e) {
      throw new TextException("Failed to parse JSON [{}] as Set", rawJson, e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set parse(String value) {
    try {
      return new LinkedHashSet(EJson.parseList(value));
    } catch (IOException e) {
      throw new PersistenceException("Failed to parse JSON content as Set: " + value, e);
    }
  }

  @Override
  public String formatValue(Set value) {
    try {
      return EJson.write(value);
    } catch (IOException e) {
      throw new PersistenceException("Failed to format Set into JSON content", e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set jsonRead(JsonReader parser) throws IOException {
    return new LinkedHashSet(EJson.parseList(parser, parser.currentToken()));
  }

  @Override
  public void jsonWrite(JsonWriter writer, Set value) throws IOException {
    EJson.write(value, writer);
  }

  /**
   * Set mapped to VARCHAR with element conversion - used as the {@code @DbArray} fallback
   * on platforms without native array support.
   */
  static final class VarcharWithConverter extends ScalarTypeJsonSet {

    private final ArrayElementConverter converter;

    VarcharWithConverter(DocPropertyType docType, boolean nullable, ArrayElementConverter converter) {
      super(Types.VARCHAR, JsonStorage.VARCHAR, docType, nullable, MutationDetection.DEFAULT);
      this.converter = converter;
    }

    @Override
    Set readJson(String rawJson) {
      return convert(rawJson);
    }

    @Override
    public Set parse(String value) {
      return convert(value);
    }

    @SuppressWarnings("unchecked")
    private Set convert(String json) {
      try {
        Set<Object> rawSet = EJson.parseSet(json, false);
        if (rawSet == null) {
          return null;
        }
        final Set result = new LinkedHashSet(rawSet.size());
        for (Object o : rawSet) {
          result.add(converter.fromSerialized(o));
        }
        return new ModifyAwareSet(result);
      } catch (IOException e) {
        throw new TextException("Failed to parse JSON [{}] as Set", json, e);
      }
    }
  }
}
