package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebean.text.json.EJson;
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

  private static final ScalarTypeJsonCollection<Set> VARCHAR = new ScalarTypeJsonSet.Varchar();

  private static final ScalarTypeJsonCollection<Set> JSON = new ScalarTypeJsonSet.Json();

  private static final ScalarTypeJsonCollection<Set> JSONB = new ScalarTypeJsonSet.JsonB();

  /**
   * Return the appropriate ScalarType for the requested dbType and Postgres.
   */
  public static ScalarType<?> typeFor(boolean postgres, int dbType) {
    if (postgres) {
      switch (dbType) {
        case DbType.JSONB: return ScalarTypeJsonSet.JSONB;
        case DbType.JSON: return ScalarTypeJsonSet.JSON;
      }
    }
    return ScalarTypeJsonSet.VARCHAR;
  }

  /**
   * List mapped to DB VARCHAR.
   */
  private static class Varchar extends ScalarTypeJsonSet.Base {
    public Varchar() {
      super(Types.VARCHAR);
    }
  }

  /**
   * List mapped to Postgres JSON.
   */
  private static class Json extends ScalarTypeJsonSet.PgBase {
    public Json() {
      super(DbType.JSON, PostgresHelper.JSON_TYPE);
    }
  }

  /**
   * List mapped to Postgres JSONB.
   */
  private static class JsonB extends ScalarTypeJsonSet.PgBase {
    public JsonB() {
      super(DbType.JSONB, PostgresHelper.JSONB_TYPE);
    }
  }

  /**
   * Base class for List handling.
   */
  private abstract static class Base extends ScalarTypeJsonCollection<Set> {

    public Base(int dbType) {
      super(Set.class, dbType);
    }

    @Override
    public Set read(DataReader dataReader) throws SQLException {
      try {
        // parse JSON into modifyAware list
        return EJson.parseSet(dataReader.getString(), true);
      } catch (IOException e) {
        throw new SQLException("Failed to parse JSON content as List: ["+ dataReader.getString() +"]", e);
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
        throw new PersistenceException("Failed to parse JSON content as Set: ["+value+"]", e);
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
      LinkedHashSet set = new LinkedHashSet();
      set.addAll(list);
      return set;
    }
  }

  /**
   * Postgres extension to base List handling.
   */
  private static class PgBase extends ScalarTypeJsonSet.Base {

    final String pgType;

    PgBase(int jdbcType, String pgType) {
      super(jdbcType);
      this.pgType = pgType;
    }

    @Override
    public void bind(DataBind bind, Set value) throws SQLException {

      String rawJson = (value == null) ? null : formatValue(value);
      bind.setObject(PostgresHelper.asObject(pgType, rawJson));
    }
  }

}
