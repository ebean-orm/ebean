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
  public static ScalarType<?> typeFor(boolean postgres, int dbType, DocPropertyType docPropertyType, boolean nullable) {
    if (postgres) {
      switch (dbType) {
        case DbPlatformType.JSONB:
          return new ScalarTypeJsonSet.JsonB(docPropertyType, nullable);
        case DbPlatformType.JSON:
          return new ScalarTypeJsonSet.Json(docPropertyType, nullable);
      }
    }
    return new ScalarTypeJsonSet.Varchar(docPropertyType, nullable);
  }

  /**
   * List mapped to DB VARCHAR.
   */
  public static class Varchar extends ScalarTypeJsonSet.Base {
    public Varchar(DocPropertyType docPropertyType, boolean nullable) {
      super(Types.VARCHAR, docPropertyType, nullable);
    }
  }

  /**
   * List mapped to Postgres JSON.
   */
  private static class Json extends ScalarTypeJsonSet.PgBase {
    public Json(DocPropertyType docPropertyType, boolean nullable) {
      super(DbPlatformType.JSON, PostgresHelper.JSON_TYPE, docPropertyType, nullable);
    }
  }

  /**
   * List mapped to Postgres JSONB.
   */
  private static class JsonB extends ScalarTypeJsonSet.PgBase {
    public JsonB(DocPropertyType docPropertyType, boolean nullable) {
      super(DbPlatformType.JSONB, PostgresHelper.JSONB_TYPE, docPropertyType, nullable);
    }
  }

  /**
   * Base class for List handling.
   */
  @SuppressWarnings("rawtypes")
  private abstract static class Base extends ScalarTypeJsonCollection<Set> {

    public Base(int dbType, DocPropertyType docPropertyType, boolean nullable) {
      super(Set.class, dbType, docPropertyType, nullable);
    }

    @Override
    public Set read(DataReader reader) throws SQLException {
      String json = reader.getString();
      try {
        // parse JSON into modifyAware list
        return EJson.parseSet(json, true);
      } catch (IOException e) {
        throw new TextException("Failed to parse JSON [{}] as Set", json, e);
      }
    }

    @Override
    public void bind(DataBinder binder, Set value) throws SQLException {
      if (value == null) {
        bindNull(binder);
      } else if (value.isEmpty()) {
        binder.setString("[]");
      } else {
        try {
          binder.setString(EJson.write(value));
        } catch (IOException e) {
          throw new SQLException("Failed to format Set into JSON content", e);
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

    PgBase(int jdbcType, String pgType, DocPropertyType docPropertyType, boolean nullable) {
      super(jdbcType, docPropertyType, nullable);
      this.pgType = pgType;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void bind(DataBinder binder, Set value) throws SQLException {
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
