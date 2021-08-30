package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Types for mapping List in JSON format to DB types VARCHAR, JSON and JSONB.
 */
final class ScalarTypeJsonList {

  /**
   * Return the appropriate ScalarType based requested dbType and if Postgres.
   */
  static ScalarType<?> typeFor(boolean postgres, int dbType, DocPropertyType docType, boolean nullable, boolean keepSource) {
    if (postgres) {
      switch (dbType) {
        case DbPlatformType.JSONB:
          return new ScalarTypeJsonList.JsonB(docType, nullable, keepSource);
        case DbPlatformType.JSON:
          return new ScalarTypeJsonList.Json(docType, nullable, keepSource);
      }
    }
    return new ScalarTypeJsonList.Varchar(docType, nullable, keepSource);
  }

  /**
   * List mapped to DB VARCHAR.
   */
  static final class Varchar extends ScalarTypeJsonList.Base {
    Varchar(DocPropertyType docType, boolean nullable, boolean keepSource) {
      super(Types.VARCHAR, docType, nullable, keepSource);
    }
  }

  /**
   * List mapped to Postgres JSON.
   */
  private final static class Json extends ScalarTypeJsonList.PgBase {
    Json(DocPropertyType docType, boolean nullable, boolean keepSource) {
      super(DbPlatformType.JSON, PostgresHelper.JSON_TYPE, docType, nullable, keepSource);
    }
  }

  /**
   * List mapped to Postgres JSONB.
   */
  private static final class JsonB extends ScalarTypeJsonList.PgBase {
    JsonB(DocPropertyType docType, boolean nullable, boolean keepSource) {
      super(DbPlatformType.JSONB, PostgresHelper.JSONB_TYPE, docType, nullable, keepSource);
    }
  }

  /**
   * Base class for List handling.
   */
  @SuppressWarnings("rawtypes")
  private abstract static class Base extends ScalarTypeJsonCollection<List> {
    final boolean keepSource;

    private Base(int dbType, DocPropertyType docType, boolean nullable, boolean keepSource) {
      super(List.class, dbType, docType, nullable);
      this.keepSource = keepSource;
    }

    @Override
    public final boolean isJsonMapper() {
      return keepSource;
    }

    @Override
    public final List read(DataReader reader) throws SQLException {
      String json = reader.getString();
      if (keepSource) {
        reader.pushJson(json);
      }
      try {
        // parse JSON into modifyAware list
        return EJson.parseList(json, true);
      } catch (IOException e) {
        throw new TextException("Failed to parse JSON [{}] as List", json, e);
      }
    }

    @Override
    public final void bind(DataBinder binder, List value) throws SQLException {
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
    public final String formatValue(List value) {
      try {
        return EJson.write(value);
      } catch (IOException e) {
        throw new PersistenceException("Failed to format List into JSON content", e);
      }
    }

    @Override
    public final List parse(String value) {
      try {
        return EJson.parseList(value, false);
      } catch (IOException e) {
        throw new TextException("Failed to parse JSON [{}] as List", value, e);
      }
    }

    @Override
    public final List jsonRead(JsonParser parser) throws IOException {
      return EJson.parseList(parser, parser.getCurrentToken());
    }

    @Override
    public final void jsonWrite(JsonGenerator writer, List value) throws IOException {
      EJson.write(value, writer);
    }
  }

  /**
   * Postgres extension to base List handling.
   */
  private static class PgBase extends ScalarTypeJsonList.Base {

    final String pgType;

    PgBase(int jdbcType, String pgType, DocPropertyType docType, boolean nullable, boolean keepSource) {
      super(jdbcType, docType, nullable, keepSource);
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
