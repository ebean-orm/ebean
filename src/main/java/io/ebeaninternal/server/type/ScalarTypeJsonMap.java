package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareOwner;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

/**
 * Type which maps Map<String,Object> to various DB types (Clob, Varchar, Blob) in JSON format.
 */
@SuppressWarnings("rawtypes")
public abstract class ScalarTypeJsonMap extends ScalarTypeBase<Map> {

  private static final ScalarTypeJsonMap CLOB = new ScalarTypeJsonMap.Clob();
  private static final ScalarTypeJsonMap BLOB = new ScalarTypeJsonMap.Blob();
  private static final ScalarTypeJsonMap VARCHAR = new ScalarTypeJsonMap.Varchar();
  private static final ScalarTypeJsonMap JSON = new ScalarTypeJsonMapPostgres.JSON();
  private static final ScalarTypeJsonMap JSONB = new ScalarTypeJsonMapPostgres.JSONB();

  /**
   * Return the ScalarType for the requested dbType and postgres.
   */
  public static ScalarTypeJsonMap typeFor(boolean postgres, int dbType) {

    switch (dbType) {
      case Types.VARCHAR:
        return VARCHAR;
      case Types.BLOB:
        return BLOB;
      case Types.CLOB:
        return CLOB;
      case DbPlatformType.JSONB:
        return postgres ? JSONB : CLOB;
      case DbPlatformType.JSON:
        return postgres ? JSON : CLOB;
      default:
        throw new IllegalStateException("Unknown dbType " + dbType);
    }
  }

  public static class Clob extends ScalarTypeJsonMap {

    public Clob() {
      super(Types.CLOB);
    }

    @Override
    public Map read(DataReader dataReader) throws SQLException {

      String content = dataReader.getStringFromStream();
      if (content == null) {
        return null;
      }
      return parse(content);
    }
  }

  public static class Varchar extends ScalarTypeJsonMap {

    public Varchar() {
      super(Types.VARCHAR);
    }
  }

  public static class Blob extends ScalarTypeJsonMap {
    public Blob() {
      super(Types.BLOB);
    }

    @Override
    public Map read(DataReader dataReader) throws SQLException {

      InputStream is = dataReader.getBinaryStream();
      if (is == null) {
        return null;
      }
      try {
        try (InputStreamReader reader = new InputStreamReader(is)) {
          return parse(reader);
        }
      } catch (IOException e) {
        throw new SQLException("Error reading Blob stream from DB", e);
      }
    }

    @Override
    public void bind(DataBind b, Map value) throws SQLException {

      if (value == null) {
        b.setNull(Types.BLOB);
      } else {
        String rawJson = formatValue(value);
        b.setBytes(rawJson.getBytes(StandardCharsets.UTF_8));
      }
    }
  }

  public ScalarTypeJsonMap(int jdbcType) {
    super(Map.class, false, jdbcType);
  }

  /**
   * Map is a mutable type. Use the isDirty() method to check for dirty state.
   */
  @Override
  public boolean isMutable() {
    return true;
  }

  /**
   * Return true if the value should be considered dirty (and included in an update).
   */
  @Override
  public boolean isDirty(Object value) {
    return !(value instanceof ModifyAwareOwner) || ((ModifyAwareOwner) value).isMarkedDirty();
  }

  @Override
  public Map read(DataReader dataReader) throws SQLException {

    String rawJson = dataReader.getString();
    if (rawJson == null) {
      return null;
    }
    return parse(rawJson);
  }

  @Override
  public void bind(DataBind b, Map value) throws SQLException {

    if (value == null) {
      b.setNull(Types.VARCHAR);
    } else {
      String rawJson = formatValue(value);
      b.setString(rawJson);
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    return value;
  }

  @Override
  public Map toBeanType(Object value) {
    return (Map) value;
  }

  @Override
  public String formatValue(Map v) {
    try {
      return EJson.write(v);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  @Override
  public Map parse(String value) {
    try {
      // return a modify aware map
      return EJson.parseObject(value, true);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  public Map parse(Reader reader) {
    try {
      // return a modify aware map
      return EJson.parseObject(reader, true);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  @Override
  public Map convertFromMillis(long dateTime) {
    throw new RuntimeException("Should never be called");
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public Map readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return parse(dataInput.readUTF());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Map map) throws IOException {
    if (map == null) {
      dataOutput.writeBoolean(false);
    } else {
      ScalarHelp.writeUTF(dataOutput, format(map));
    }
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Map value) throws IOException {
    EJson.write(value, writer);
  }

  @Override
  public Map jsonRead(JsonParser parser) throws IOException {
    return EJson.parseObject(parser, parser.getCurrentToken());
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.OBJECT;
  }

}
