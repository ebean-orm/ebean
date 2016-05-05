package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebean.text.json.EJson;
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
public class ScalarTypeJsonList  {

  public static final ScalarTypeJsonCollection<List> VARCHAR = new ScalarTypeJsonList.Varchar();

  public static final ScalarTypeJsonCollection<List> JSON = new ScalarTypeJsonList.Json();

  public static final ScalarTypeJsonCollection<List> JSONB = new ScalarTypeJsonList.JsonB();

  /**
   * List mapped to DB VARCHAR.
   */
  private static class Varchar extends ScalarTypeJsonList.Base {
    public Varchar() {
      super(Types.VARCHAR);
    }
  }

  /**
   * List mapped to Postgres JSON.
   */
  private static class Json extends ScalarTypeJsonList.PgBase {
    public Json() {
      super(DbType.JSON, PostgresHelper.JSON_TYPE);
    }
  }

  /**
   * List mapped to Postgres JSONB.
   */
  private static class JsonB extends ScalarTypeJsonList.PgBase {
    public JsonB() {
      super(DbType.JSONB, PostgresHelper.JSONB_TYPE);
    }
  }

  /**
   * Base class for List handling.
   */
  private abstract static class Base extends ScalarTypeJsonCollection<List> {

    public Base(int dbType) {
      super(List.class, dbType);
    }

    @Override
    public List read(DataReader dataReader) throws SQLException {
      try {
        // parse JSON into modifyAware list
        return EJson.parseList(dataReader.getString(), true);
      } catch (IOException e) {
        throw new SQLException("Failed to parse JSON content as List: ["+ dataReader.getString() +"]", e);
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
        throw new PersistenceException("Failed to parse JSON content as List: ["+value+"]", e);
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

    PgBase(int jdbcType, String pgType) {
      super(jdbcType);
      this.pgType = pgType;
    }

    @Override
    public void bind(DataBind bind, List value) throws SQLException {

      String rawJson = (value == null) ? null : formatValue(value);
      bind.setObject(PostgresHelper.asObject(pgType, rawJson));
    }
  }

}
