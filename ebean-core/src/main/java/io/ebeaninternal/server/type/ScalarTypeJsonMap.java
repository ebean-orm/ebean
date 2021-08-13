package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

/**
 * Type which maps Map<String,Object> to various DB types (Clob, Varchar, Blob) in JSON format.
 */
@SuppressWarnings("rawtypes")
public abstract class ScalarTypeJsonMap extends ScalarTypeBase<Map> {

  /**
   * Return the ScalarType for the requested dbType and postgres.
   */
  public static ScalarTypeJsonMap typeFor(boolean postgres, int dbType, boolean keepSource) {
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

  public static class Clob extends ScalarTypeJsonMap {

    public Clob(boolean keepSource) {
      super(Types.CLOB, keepSource);
    }

    @Override
    protected String readJson(DataReader reader) throws SQLException {
      return reader.getStringFromStream();
    }
  }

  public static class Varchar extends ScalarTypeJsonMap {

    public Varchar(boolean keepSource) {
      super(Types.VARCHAR, keepSource);
    }
  }

  public static class Blob extends ScalarTypeJsonMap {
    public Blob(boolean keepSource) {
      super(Types.BLOB, keepSource);
    }

    @Override
    public Map read(DataReader reader) throws SQLException {
      InputStream is = reader.getBinaryStream();
      if (is == null) {
        if (isJsonMapper()) {
          reader.pushJson(null);
        }
        return null;
      }
      try {
        if (isJsonMapper()) {
          StringWriter rawJson = new StringWriter();
           try (InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
             inputStreamReader.transferTo(rawJson);
          }
           reader.pushJson(rawJson.toString());
           return parse(rawJson.toString());
          
        } else {
          try (InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return parse(inputStreamReader);
          }
        }
      } catch (IOException e) {
        throw new SQLException("Error reading Blob stream from DB", e);
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

  private final boolean keepSource;
  
  public ScalarTypeJsonMap(int jdbcType, boolean keepSource) {
    super(Map.class, false, jdbcType);
    this.keepSource = keepSource;
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
    return TypeJsonManager.checkIsDirty(value);
  }
  
 
  
  @Override
  public boolean isJsonMapper() {
    return keepSource;
  }
  

  @Override
  public Map read(DataReader reader) throws SQLException {
    String rawJson = readJson(reader);
    if (isJsonMapper()) {
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
    String rawJson = isJsonMapper() ? binder.popJson() : null;
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
      throw new TextException("Failed to parse JSON [{}] as Object", value, e);
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
