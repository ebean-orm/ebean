package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.text.json.EJson;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Types for mapping List in JSON format to DB types VARCHAR, JSON and JSONB.
 */
public class ScalarTypeJsonList {
  /**
   * Return the appropriate ScalarType based requested dbType and if Postgres.
   */
  public static ScalarType<?> typeFor(boolean postgres, int dbType, DocPropertyType docType) {
    if (postgres) {
      switch (dbType) {
        case DbPlatformType.JSONB:
          return new ScalarTypeJsonList.JsonB(docType);
        case DbPlatformType.JSON:
          return new ScalarTypeJsonList.Json(docType);
      }
    }
    return new ScalarTypeJsonList.Varchar(docType);
  }

  /**
   * List mapped to DB VARCHAR.
   */
  public static class Varchar extends ScalarTypeJsonList.Base {
    public Varchar(DocPropertyType docType) {
      super(Types.VARCHAR, docType);
    }
  }

  /**
   * List mapped to Postgres JSON.
   */
  private static class Json extends ScalarTypeJsonList.PgBase {
    public Json(DocPropertyType docType) {
      super(DbPlatformType.JSON, PostgresHelper.JSON_TYPE, docType);
    }
  }

  /**
   * List mapped to Postgres JSONB.
   */
  private static class JsonB extends ScalarTypeJsonList.PgBase {
    public JsonB(DocPropertyType docType) {
      super(DbPlatformType.JSONB, PostgresHelper.JSONB_TYPE, docType);
    }
  }

  /**
   * Base class for List handling.
   */
  @SuppressWarnings("rawtypes")
  private abstract static class Base extends ScalarTypeJsonCollection<List> {

    public Base(int dbType, DocPropertyType docType) {
      super(List.class, dbType, docType);
    }

    @Override
    public List read(DataReader dataReader) throws SQLException {
      String json = dataReader.getString();
      try {
        // parse JSON into modifyAware list
        return EJson.parseList(json, true);
      } catch (IOException e) {
        throw new SQLException("Failed to parse JSON content as List: [" + json + "]", e);
      }
    }

    @Override
    public void bind(DataBind b, List value) throws SQLException {

      if (value == null) {
        b.setNull(Types.VARCHAR);
      } else if (value.isEmpty()) {
        b.setString("[]");
      } else {
        try {
          b.setString(EJson.write(value));
        } catch (IOException e) {
          throw new SQLException("Failed to format List into JSON content", e);
        }
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
    public List parse(String value) {
      try {
        return EJson.parseList(value, false);
      } catch (IOException e) {
        throw new PersistenceException("Failed to parse JSON content as List: [" + value + "]", e);
      }
    }

    @Override
    public List jsonRead(JsonParser parser) throws IOException {
      return EJson.parseList(parser, parser.getCurrentToken());
    }

    @Override
    public void jsonWrite(JsonGenerator writer, List value) throws IOException {
      EJson.write(value, writer);
    }

  }

  /**
   * Postgres extension to base List handling.
   */
  private static class PgBase extends ScalarTypeJsonList.Base {

    final String pgType;

    PgBase(int jdbcType, String pgType, DocPropertyType docType) {
      super(jdbcType, docType);
      this.pgType = pgType;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void bind(DataBind bind, List value) throws SQLException {

      String rawJson = (value == null) ? null : formatValue(value);
      bind.setObject(PostgresHelper.asObject(pgType, rawJson));
    }
  }

}
