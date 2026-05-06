package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.*;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;
import io.ebean.util.IOUtils;
import io.ebeaninternal.json.ModifyAwareMap;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Type which maps Map<Enum,Object> to various DB types (Clob, Varchar, Blob) in JSON format.
 * The enum keys are serialized using their ScalarType (which may use custom enum values).
 */
@SuppressWarnings("rawtypes")
abstract class ScalarTypeJsonMapEnum<T extends Enum<T>> extends ScalarTypeBase<Map> {

  final boolean keepSource;
  private final ScalarType<T> enumScalarType;

  ScalarTypeJsonMapEnum(int jdbcType, ScalarType<T> enumScalarType, boolean keepSource) {
    super(Map.class, false, jdbcType);
    this.enumScalarType = enumScalarType;
    this.keepSource = keepSource;
  }

  /**
   * Return the ScalarType for the requested dbType and postgres.
   */
  static <T extends Enum<T>> ScalarTypeJsonMapEnum<T> typeFor(boolean postgres, int dbType, ScalarType<T> enumScalarType, boolean keepSource) {
    switch (dbType) {
      case Types.VARCHAR:
        return new ScalarTypeJsonMapEnum.Varchar<>(enumScalarType, keepSource);
      case Types.BLOB:
        return new ScalarTypeJsonMapEnum.Blob<>(enumScalarType, keepSource);
      case Types.CLOB:
        return new ScalarTypeJsonMapEnum.Clob<>(enumScalarType, keepSource);
      case DbPlatformType.JSONB:
        return postgres ? new ScalarTypeJsonMapEnumPostgres.JSONB<>(enumScalarType, keepSource) : new ScalarTypeJsonMapEnum.Clob<>(enumScalarType, keepSource);
      case DbPlatformType.JSON:
        return postgres ? new ScalarTypeJsonMapEnumPostgres.JSON<>(enumScalarType, keepSource) : new ScalarTypeJsonMapEnum.Clob<>(enumScalarType, keepSource);
      default:
        throw new IllegalStateException("Unknown dbType " + dbType);
    }
  }

  /**
   * Map is a mutable type. Use the isDirty() method to check for dirty state.
   */
  @Override
  public final boolean mutable() {
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
  public final boolean jsonMapper() {
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

  /**
   * Convert Map<Enum,Object> to JSON string.
   * Enum keys are serialized using the enumScalarType's formatValue method.
   */
  @Override
  @SuppressWarnings("unchecked")
  public final String formatValue(Map v) {
    try {
      // Convert enum keys to their string representation
      Map<String, Object> stringKeyMap = new LinkedHashMap<>();
      for (Object entry : v.entrySet()) {
        Map.Entry e = (Map.Entry) entry;
        T mapKey = (T) e.getKey();
        String key = enumScalarType.formatValue(mapKey);
        stringKeyMap.put(key, e.getValue());
      }
      return EJson.write(stringKeyMap);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  /**
   * Parse JSON string to Map<Enum,Object>.
   * String keys are parsed back to enum values using the enumScalarType's parse method.
   */
  public final Map parse(String value) {
    try {
      Map<String, Object> stringKeyMap = EJson.parseObject(value, true);
      return convertToEnumKeyMap(stringKeyMap);
    } catch (IOException e) {
      throw new TextException("Failed to parse JSON [{}] as Map with enum keys", value, e);
    }
  }

  public final Map parse(Reader reader) {
    try {
      Map<String, Object> stringKeyMap = EJson.parseObject(reader, true);
      return convertToEnumKeyMap(stringKeyMap);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  /**
   * Convert a Map<String, Object> to Map<Enum, Object> by parsing string keys to enum values.
   */
  @SuppressWarnings("unchecked")
  private Map convertToEnumKeyMap(Map<String, Object> stringKeyMap) {
    if (stringKeyMap == null) {
      return null;
    }

    boolean modifyAware = stringKeyMap instanceof ModifyAwareMap;

    Map newMap = new HashMap<>(stringKeyMap);
    for (Map.Entry<String, Object> entry : stringKeyMap.entrySet()) {
      Object enumKey = enumScalarType.parse(entry.getKey());
      newMap.remove(entry.getKey());
      newMap.put(enumKey, entry.getValue());
    }
    return modifyAware ? new ModifyAwareMap(newMap) : newMap;
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
  @SuppressWarnings("unchecked")
  public final void jsonWrite(JsonGenerator writer, Map value) throws IOException {
    if (value == null) {
      writer.writeNull();
    } else {
      writer.writeStartObject();
      for (Object entry : value.entrySet()) {
        Map.Entry e = (Map.Entry) entry;
        T mapKey = (T) e.getKey();
        String key = enumScalarType.formatValue(mapKey);
        writer.writeFieldName(key);
        writer.writeObject(e.getValue());
      }
      writer.writeEndObject();
    }
  }

  @Override
  public final Map jsonRead(JsonParser parser) throws IOException {
    JsonToken token = parser.getCurrentToken();
    if (token == JsonToken.VALUE_NULL) {
      return null;
    }
    Map<String, Object> stringKeyMap = EJson.parseObject(parser, token);
    return convertToEnumKeyMap(stringKeyMap);
  }

  @Override
  public final DocPropertyType docType() {
    return DocPropertyType.OBJECT;
  }

  private static final class Clob<T extends Enum<T>> extends ScalarTypeJsonMapEnum<T> {
    Clob(ScalarType<T> enumScalarType, boolean keepSource) {
      super(Types.CLOB, enumScalarType, keepSource);
    }

    @Override
    protected String readJson(DataReader reader) throws SQLException {
      return reader.getStringFromStream();
    }
  }

  private static final class Varchar<T extends Enum<T>> extends ScalarTypeJsonMapEnum<T> {
    Varchar(ScalarType<T> enumScalarType, boolean keepSource) {
      super(Types.VARCHAR, enumScalarType, keepSource);
    }
  }

  private static final class Blob<T extends Enum<T>> extends ScalarTypeJsonMapEnum<T> {
    Blob(ScalarType<T> enumScalarType, boolean keepSource) {
      super(Types.BLOB, enumScalarType, keepSource);
    }

    private static void transferTo(Reader reader, Writer out) throws IOException {
      char[] buffer = new char[2048];
      int nRead;
      while ((nRead = reader.read(buffer, 0, 2048)) >= 0) {
        out.write(buffer, 0, nRead);
      }
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

    @Override
    protected void bindNull(DataBinder binder) throws SQLException {
      binder.setNull(Types.BLOB);
    }

    @Override
    protected void bindJson(DataBinder binder, String rawJson) throws SQLException {
      binder.setBytes(rawJson.getBytes(StandardCharsets.UTF_8));
    }
  }
}
