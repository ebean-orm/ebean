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
public class ScalarTypeJsonList {

  /**
   * Return the appropriate ScalarType based requested dbType and if Postgres.
   */
  public static ScalarType<?> typeFor(boolean postgres, int dbType, DocPropertyType docType, boolean nullable) {
    if (postgres) {
      switch (dbType) {
        case DbPlatformType.JSONB:
          return new ScalarTypeJsonList.JsonB(docType, nullable);
        case DbPlatformType.JSON:
          return new ScalarTypeJsonList.Json(docType, nullable);
      }
    }
    return new ScalarTypeJsonList.Varchar(docType, nullable);
  }

  /**
   * List mapped to DB VARCHAR.
   */
  public static class Varchar extends ScalarTypeJsonList.Base {
    public Varchar(DocPropertyType docType, boolean nullable) {
      super(Types.VARCHAR, docType, nullable);
    }
  }

  /**
   * List mapped to Postgres JSON.
   */
  private static class Json extends ScalarTypeJsonList.PgBase {
    public Json(DocPropertyType docType, boolean nullable) {
      super(DbPlatformType.JSON, PostgresHelper.JSON_TYPE, docType, nullable);
    }
  }

  /**
   * List mapped to Postgres JSONB.
   */
  private static class JsonB extends ScalarTypeJsonList.PgBase {
    public JsonB(DocPropertyType docType, boolean nullable) {
      super(DbPlatformType.JSONB, PostgresHelper.JSONB_TYPE, docType, nullable);
    }
  }

  /**
   * Base class for List handling.
   */
  @SuppressWarnings("rawtypes")
  private abstract static class Base extends ScalarTypeJsonCollection<List> {

    public Base(int dbType, DocPropertyType docType, boolean nullable) {
      super(List.class, dbType, docType, nullable);
    }

    @Override
    public List read(DataReader reader) throws SQLException {
      String json = reader.getString();
      try {
        // parse JSON into modifyAware list
        return EJson.parseList(json, true);
      } catch (IOException e) {
        throw new TextException("Failed to parse JSON [{}] as List", json, e);
      }
    }

    @Override
    public void bind(DataBinder binder, List value) throws SQLException {
      if (value == null) {
        bindNull(binder);
      } else if (value.isEmpty()) {
        binder.setString("[]");
      } else {
        try {
          binder.setString(EJson.write(value));
        } catch (IOException e) {
          throw new SQLException("Failed to format List into JSON content", e);
        }
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
        throw new TextException("Failed to parse JSON [{}] as List", value, e);
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

    PgBase(int jdbcType, String pgType, DocPropertyType docType, boolean nullable) {
      super(jdbcType, docType, nullable);
      this.pgType = pgType;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void bind(DataBinder binder, List value) throws SQLException {
      if (value == null) {
        bindNull(binder);
      } else {
        binder.setObject(PostgresHelper.asObject(pgType, formatValue(value)));
      }
    }

    @Override
    protected void bindNull(DataBinder binder) throws SQLException {
      binder.setObject(PostgresHelper.asObject(pgType, nullable ? null : "[]"));
    }
  }

}
