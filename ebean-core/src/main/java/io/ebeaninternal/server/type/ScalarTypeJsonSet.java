package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.*;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareSet;

import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Types for mapping List in JSON format to DB types VARCHAR, JSON and JSONB.
 */
final class ScalarTypeJsonSet {

  /**
   * Return the appropriate ScalarType for the requested dbType and Postgres.
   */
  static ScalarType<?> typeFor(boolean postgres, int dbType, DocPropertyType docPropertyType, boolean nullable, boolean keepSource) {
    if (postgres) {
      switch (dbType) {
        case DbPlatformType.JSONB:
          return new ScalarTypeJsonSet.JsonB(docPropertyType, nullable, keepSource);
        case DbPlatformType.JSON:
          return new ScalarTypeJsonSet.Json(docPropertyType, nullable, keepSource);
      }
    }
    return new ScalarTypeJsonSet.Varchar(docPropertyType, nullable, keepSource);
  }

  @SuppressWarnings("rawtypes")
  static final class VarcharWithConverter extends ScalarTypeJsonSet.Base {
    private final ArrayElementConverter converter;

    VarcharWithConverter(DocPropertyType docType, boolean nullable, boolean keepSource, ArrayElementConverter converter) {
      super(Types.VARCHAR, docType, nullable, keepSource);
      this.converter = converter;
    }

    @Override
    Set readJsonConvert(String json) {
      try {
        return convertElements(EJson.parseSet(json, false));
      } catch (IOException e) {
        throw new TextException("Failed to parse JSON [{}] as List", json, e);
      }
    }

    @SuppressWarnings("unchecked")
    private Set convertElements(Set<Object> rawSet) {
      if (rawSet == null) {
        return null;
      }
      final Set result = new LinkedHashSet(rawSet.size());
      for (Object o : rawSet) {
        result.add(converter.fromSerialized(o));
      }
      return new ModifyAwareSet(result);
    }

    @Override
    public Set parse(String value) {
      try {
        return convertElements(EJson.parseSet(value, false));
      } catch (IOException e) {
        throw new PersistenceException("Failed to parse JSON content as Set: " + value, e);
      }
    }
  }
  /**
   * List mapped to DB VARCHAR.
   */
  static final class Varchar extends ScalarTypeJsonSet.Base {
    public Varchar(DocPropertyType docPropertyType, boolean nullable, boolean keepSource) {
      super(Types.VARCHAR, docPropertyType, nullable, keepSource);
    }
  }

  /**
   * List mapped to Postgres JSON.
   */
  private static final class Json extends ScalarTypeJsonSet.PgBase {
    private Json(DocPropertyType docPropertyType, boolean nullable, boolean keepSource) {
      super(DbPlatformType.JSON, PostgresHelper.JSON_TYPE, docPropertyType, nullable, keepSource);
    }
  }

  /**
   * List mapped to Postgres JSONB.
   */
  private static final class JsonB extends ScalarTypeJsonSet.PgBase {
    private JsonB(DocPropertyType docPropertyType, boolean nullable, boolean keepSource) {
      super(DbPlatformType.JSONB, PostgresHelper.JSONB_TYPE, docPropertyType, nullable, keepSource);
    }
  }

  @SuppressWarnings("rawtypes")
  private abstract static class Base extends ScalarTypeJsonCollection<Set> {

    final boolean keepSource;

    private Base(int dbType, DocPropertyType docPropertyType, boolean nullable, boolean keepSource) {
      super(Set.class, dbType, docPropertyType, nullable);
      this.keepSource = keepSource;
    }

    @Override
    public final boolean jsonMapper() {
      return keepSource;
    }

    @Override
    public final Set read(DataReader reader) throws SQLException {
      String json = reader.getString();
      if (keepSource) {
        reader.pushJson(json);
      }
      return readJsonConvert(json);
    }

    Set readJsonConvert(String json) {
      try {
        return EJson.parseSet(json, true);
      } catch (IOException e) {
        throw new TextException("Failed to parse JSON [{}] as Set", json, e);
      }
    }

    @Override
    public final void bind(DataBinder binder, Set value) throws SQLException {
      String rawJson = keepSource ? binder.popJson() : null;
      if (rawJson == null && value != null) {
        rawJson = formatValue(value);
      }
      if (value == null) {
        bindNull(binder);
      } else {
        bindRawJson(binder, rawJson);
      }
    }

    @Override
    protected void bindNull(DataBinder binder) throws SQLException {
      if (nullable) {
        binder.setNull(Types.VARCHAR);
      } else {
        binder.setString("[]");
      }
    }

    protected void bindRawJson(DataBinder binder, String rawJson) throws SQLException {
      binder.setString(rawJson);
    }

    @Override
    public final String formatValue(Set value) {
      try {
        return EJson.write(value);
      } catch (IOException e) {
        throw new PersistenceException("Failed to format List into JSON content", e);
      }
    }

    @Override
    public Set parse(String value) {
      try {
        return convertList(EJson.parseList(value));
      } catch (IOException e) {
        throw new PersistenceException("Failed to parse JSON content as Set: " + value, e);
      }
    }

    @Override
    public final Set jsonRead(JsonParser parser) throws IOException {
      return convertList(EJson.parseList(parser, parser.getCurrentToken()));
    }

    @Override
    public final void jsonWrite(JsonGenerator writer, Set value) throws IOException {
      EJson.write(value, writer);
    }

    @SuppressWarnings("unchecked")
    private Set convertList(List list) {
      if (list == null) {
        return null;
      }
      return new LinkedHashSet(list);
    }
  }

  /**
   * Postgres extension to base List handling.
   */
  private static class PgBase extends ScalarTypeJsonSet.Base {

    final String pgType;

    PgBase(int jdbcType, String pgType, DocPropertyType docPropertyType, boolean nullable, boolean keepSource) {
      super(jdbcType, docPropertyType, nullable, keepSource);
      this.pgType = pgType;
    }

    @Override
    protected final void bindRawJson(DataBinder binder, String rawJson) throws SQLException {
      binder.setObject(PostgresHelper.asObject(pgType, rawJson));
    }

    @Override
    protected final void bindNull(DataBinder binder) throws SQLException {
      binder.setObject(PostgresHelper.asObject(pgType, nullable ? null : "[]"));
    }
  }

}
