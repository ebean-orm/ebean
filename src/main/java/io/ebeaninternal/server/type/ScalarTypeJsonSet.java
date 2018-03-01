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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Types for mapping List in JSON format to DB types VARCHAR, JSON and JSONB.
 */
public class ScalarTypeJsonSet {

  /**
   * Return the appropriate ScalarType for the requested dbType and Postgres.
   */
  public static ScalarType<?> typeFor(boolean postgres, int dbType, DocPropertyType docPropertyType) {
    if (postgres) {
      switch (dbType) {
        case DbPlatformType.JSONB:
          return new ScalarTypeJsonSet.JsonB(docPropertyType);
        case DbPlatformType.JSON:
          return new ScalarTypeJsonSet.Json(docPropertyType);
      }
    }
    return new ScalarTypeJsonSet.Varchar(docPropertyType);
  }

  /**
   * List mapped to DB VARCHAR.
   */
  public static class Varchar extends ScalarTypeJsonSet.Base {
    public Varchar(DocPropertyType docPropertyType) {
      super(Types.VARCHAR, docPropertyType);
    }
  }

  /**
   * List mapped to Postgres JSON.
   */
  private static class Json extends ScalarTypeJsonSet.PgBase {
    public Json(DocPropertyType docPropertyType) {
      super(DbPlatformType.JSON, PostgresHelper.JSON_TYPE, docPropertyType);
    }
  }

  /**
   * List mapped to Postgres JSONB.
   */
  private static class JsonB extends ScalarTypeJsonSet.PgBase {
    public JsonB(DocPropertyType docPropertyType) {
      super(DbPlatformType.JSONB, PostgresHelper.JSONB_TYPE, docPropertyType);
    }
  }

  /**
   * Base class for List handling.
   */
  @SuppressWarnings("rawtypes")
  private abstract static class Base extends ScalarTypeJsonCollection<Set> {

    public Base(int dbType, DocPropertyType docPropertyType) {
      super(Set.class, dbType, docPropertyType);
    }

    @Override
    public Set read(DataReader dataReader) throws SQLException {
      String json = dataReader.getString();
      try {
        // parse JSON into modifyAware list
        return EJson.parseSet(json, true);
      } catch (IOException e) {
        throw new SQLException("Failed to parse JSON content as List: [" + json + "]", e);
      }
    }

    @Override
    public void bind(DataBind b, Set value) throws SQLException {

      if (value == null) {
        b.setNull(Types.VARCHAR);
      } else if (value.isEmpty()) {
        b.setString("[]");
      } else {
        try {
          b.setString(EJson.write(value));
        } catch (IOException e) {
          throw new SQLException("Failed to format Set into JSON content", e);
        }
      }
    }

    @Override
    public String formatValue(Set value) {
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
        throw new PersistenceException("Failed to parse JSON content as Set: [" + value + "]", e);
      }
    }

    @Override
    public Set jsonRead(JsonParser parser) throws IOException {
      return convertList(EJson.parseList(parser, parser.getCurrentToken()));
    }

    @Override
    public void jsonWrite(JsonGenerator writer, Set value) throws IOException {
      EJson.write(value, writer);
    }

    @SuppressWarnings("unchecked")
    private Set convertList(List list) {
      return new LinkedHashSet(list);
    }
  }

  /**
   * Postgres extension to base List handling.
   */
  private static class PgBase extends ScalarTypeJsonSet.Base {

    final String pgType;

    PgBase(int jdbcType, String pgType, DocPropertyType docPropertyType) {
      super(jdbcType, docPropertyType);
      this.pgType = pgType;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void bind(DataBind bind, Set value) throws SQLException {

      String rawJson = (value == null) ? null : formatValue(value);
      bind.setObject(PostgresHelper.asObject(pgType, rawJson));
    }
  }

}
