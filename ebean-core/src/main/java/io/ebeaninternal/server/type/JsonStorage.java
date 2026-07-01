package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.PostgresHelper;
import io.ebean.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Strategy for reading and binding the raw JSON string of a {@code @DbJson} property.
 * <p>
 * This collapses the previously duplicated VARCHAR / CLOB / BLOB and Postgres JSON / JSONB
 * variants (which differed only in how the raw JSON is read from / bound to JDBC) into a
 * small set of reusable strategies shared by all JSON value types (Map, List, Set).
 */
interface JsonStorage {

  /** VARCHAR storage - read/bind as a String. */
  JsonStorage VARCHAR = new Varchar();
  /** CLOB storage - read via stream, bind as a String. */
  JsonStorage CLOB = new Clob();
  /** BLOB storage - read/bind as UTF-8 bytes. */
  JsonStorage BLOB = new Blob();

  /** Postgres JSON / JSONB storage binding via a PGobject of the given type. */
  static JsonStorage postgres(String pgType) {
    return new Postgres(pgType);
  }

  /**
   * Read the raw JSON content (or null) from the DB.
   */
  String read(DataReader reader) throws SQLException;

  /**
   * The JDBC type reported by a ScalarType using this storage.
   */
  int jdbcType();

  /**
   * Bind the given non-null raw JSON content.
   */
  void bind(DataBinder binder, String rawJson) throws SQLException;

  /**
   * Bind an SQL null value.
   */
  void bindNull(DataBinder binder) throws SQLException;

  final class Varchar implements JsonStorage {
    @Override
    public String read(DataReader reader) throws SQLException {
      return reader.getString();
    }

    @Override
    public int jdbcType() {
      return Types.VARCHAR;
    }

    @Override
    public void bind(DataBinder binder, String rawJson) throws SQLException {
      binder.setString(rawJson);
    }

    @Override
    public void bindNull(DataBinder binder) throws SQLException {
      binder.setNull(Types.VARCHAR);
    }
  }

  final class Clob implements JsonStorage {
    @Override
    public String read(DataReader reader) throws SQLException {
      return reader.getStringFromStream();
    }

    @Override
    public int jdbcType() {
      return Types.CLOB;
    }

    @Override
    public void bind(DataBinder binder, String rawJson) throws SQLException {
      binder.setString(rawJson);
    }

    @Override
    public void bindNull(DataBinder binder) throws SQLException {
      binder.setNull(Types.VARCHAR);
    }
  }

  final class Blob implements JsonStorage {
    @Override
    public String read(DataReader reader) throws SQLException {
      InputStream is = reader.getBinaryStream();
      if (is == null) {
        return null;
      }
      try (Reader streamReader = IOUtils.newReader(is)) {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[2048];
        int nRead;
        while ((nRead = streamReader.read(buffer)) >= 0) {
          builder.append(buffer, 0, nRead);
        }
        return builder.toString();
      } catch (IOException e) {
        throw new SQLException("Error reading Blob stream from DB", e);
      }
    }

    @Override
    public int jdbcType() {
      return Types.BLOB;
    }

    @Override
    public void bind(DataBinder binder, String rawJson) throws SQLException {
      binder.setBytes(rawJson.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void bindNull(DataBinder binder) throws SQLException {
      binder.setNull(Types.BLOB);
    }
  }

  final class Postgres implements JsonStorage {
    private final String pgType;
    private final int jdbcType;

    Postgres(String pgType) {
      this.pgType = pgType;
      this.jdbcType = PostgresHelper.JSONB_TYPE.equals(pgType) ? DbPlatformType.JSONB : DbPlatformType.JSON;
    }

    @Override
    public String read(DataReader reader) throws SQLException {
      return reader.getString();
    }

    @Override
    public int jdbcType() {
      return jdbcType;
    }

    @Override
    public void bind(DataBinder binder, String rawJson) throws SQLException {
      binder.setObject(PostgresHelper.asObject(pgType, rawJson));
    }

    @Override
    public void bindNull(DataBinder binder) throws SQLException {
      binder.setObject(PostgresHelper.asObject(pgType, null));
    }
  }
}
