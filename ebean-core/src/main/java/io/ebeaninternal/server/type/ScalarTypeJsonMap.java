package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;
import io.ebean.util.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

/**
 * Type which maps Map<String,Object> to various DB types (Clob, Varchar, Blob) in JSON format.
 */
@SuppressWarnings("rawtypes")
abstract class ScalarTypeJsonMap extends ScalarTypeBase<Map> {

  /**
   * Return the ScalarType for the requested dbType and postgres.
   */
  static ScalarTypeJsonMap typeFor(boolean postgres, int dbType, boolean keepSource) {
    switch (dbType) {
      case Types.VARCHAR:
        return new ScalarTypeJsonMap.Varchar(keepSource);
      case Types.BLOB:
        return new ScalarTypeJsonMap.Blob(keepSource);
      case Types.CLOB:
        return new ScalarTypeJsonMap.Clob(keepSource);
      case DbPlatformType.JSONB:
        return postgres ? new ScalarTypeJsonMapPostgres.JSONB(keepSource) : new ScalarTypeJsonMap.Clob(keepSource);
      case DbPlatformType.JSON:
        return postgres ? new ScalarTypeJsonMapPostgres.JSON(keepSource) : new ScalarTypeJsonMap.Clob(keepSource);
      default:
        throw new IllegalStateException("Unknown dbType " + dbType);
    }
  }

  private static final class Clob extends ScalarTypeJsonMap {
    Clob(boolean keepSource) {
      super(Types.CLOB, keepSource);
    }

    @Override
    protected String readJson(DataReader reader) throws SQLException {
      return reader.getStringFromStream();
    }
  }

  private static final class Varchar extends ScalarTypeJsonMap {
    Varchar(boolean keepSource) {
      super(Types.VARCHAR, keepSource);
    }
  }

  private static final class Blob extends ScalarTypeJsonMap {
    Blob(boolean keepSource) {
      super(Types.BLOB, keepSource);
    }

    @Override
    public Map read(DataReader reader) throws SQLException {
      InputStream is = reader.getBinaryStream();
      if (is == null) {
        if (keepSource) {
          reader.pushJson(null);
        }
        return null;
      }
      try {
        if (keepSource) {
          StringWriter jsonBuffer = new StringWriter();
          try (Reader streamReader = IOUtils.newReader(is)) {
            transferTo(streamReader, jsonBuffer);
          }
          String rawJson = jsonBuffer.toString();
          reader.pushJson(rawJson);
          return parse(rawJson);
        } else {
          try (Reader streamReader = IOUtils.newReader(is)) {
            return parse(streamReader);
          }
        }
      } catch (IOException e) {
        throw new SQLException("Error reading Blob stream from DB", e);
      }
    }

    private static void transferTo(Reader reader, Writer out) throws IOException {
      char[] buffer = new char[2048];
      int nRead;
      while ((nRead = reader.read(buffer, 0, 2048)) >= 0) {
        out.write(buffer, 0, nRead);
      }
    }

    @Override
    protected void bindNull(DataBinder binder) throws SQLException {
      binder.setNull(Types.BLOB);
    }

    @Override
    protected void bindJson(DataBinder binder, String rawJson) throws SQLException {
      binder.setBytes(rawJson.getBytes(StandardCharsets.UTF_8));
    }

  }

  final boolean keepSource;

  ScalarTypeJsonMap(int jdbcType, boolean keepSource) {
    super(Map.class, false, jdbcType);
    this.keepSource = keepSource;
  }

  /**
   * Map is a mutable type. Use the isDirty() method to check for dirty state.
   */
  @Override
  public final boolean isMutable() {
    return true;
  }

  /**
   * Return true if the value should be considered dirty (and included in an update).
   */
  @Override
  public boolean isDirty(Object value) {
    return TypeJsonManager.checkIsDirty(value);
  }

  @Override
  public final boolean isJsonMapper() {
    return keepSource;
  }

  @Override
  public Map read(DataReader reader) throws SQLException {
    String rawJson = readJson(reader);
    if (keepSource) {
      reader.pushJson(rawJson);
    }
    if (rawJson == null) {
      return null;
    }
    return parse(rawJson);
  }

  protected String readJson(DataReader reader) throws SQLException {
    return reader.getString();
  }

  @Override
  public final void bind(DataBinder binder, Map value) throws SQLException {
    String rawJson = keepSource ? binder.popJson() : null;
    if (rawJson == null && value != null) {
      rawJson = formatValue(value);
    }
    if (value == null) {
      bindNull(binder);
    } else {
      bindJson(binder, rawJson);
    }
  }

  protected void bindNull(DataBinder binder) throws SQLException {
    binder.setNull(Types.VARCHAR);
  }

  protected void bindJson(DataBinder binder, String rawJson) throws SQLException {
    binder.setString(rawJson);
  }

  @Override
  public final Object toJdbcType(Object value) {
    return value;
  }

  @Override
  public final Map toBeanType(Object value) {
    return (Map) value;
  }

  @Override
  public final String formatValue(Map v) {
    try {
      return EJson.write(v);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  @Override
  public final Map parse(String value) {
    try {
      // return a modify aware map
      return EJson.parseObject(value, true);
    } catch (IOException e) {
      throw new TextException("Failed to parse JSON [{}] as Object", value, e);
    }
  }

  public final Map parse(Reader reader) {
    try {
      // return a modify aware map
      return EJson.parseObject(reader, true);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  @Override
  public final Map readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return parse(dataInput.readUTF());
    }
  }

  @Override
  public final void writeData(DataOutput dataOutput, Map map) throws IOException {
    if (map == null) {
      dataOutput.writeBoolean(false);
    } else {
      ScalarHelp.writeUTF(dataOutput, format(map));
    }
  }

  @Override
  public final void jsonWrite(JsonGenerator writer, Map value) throws IOException {
    EJson.write(value, writer);
  }

  @Override
  public final Map jsonRead(JsonParser parser) throws IOException {
    return EJson.parseObject(parser, parser.getCurrentToken());
  }

  @Override
  public final DocPropertyType getDocType() {
    return DocPropertyType.OBJECT;
  }

}
