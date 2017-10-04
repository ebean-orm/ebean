package io.ebeaninternal.server.type;

import io.ebean.config.dbplatform.DbPlatformType;
import io.ebeaninternal.json.ModifyAwareList;
import io.ebeaninternal.json.ModifyAwareMap;
import io.ebeaninternal.json.ModifyAwareOwner;
import io.ebeaninternal.json.ModifyAwareSet;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.PersistenceException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Supports @DbJson properties using Jackson ObjectMapper.
 */
public class ScalarTypeJsonObjectMapper {

  /**
   * Create and return the appropriate ScalarType.
   */
  public static ScalarType<?> createTypeFor(boolean postgres, Class<?> type, ObjectMapper objectMapper,
                                            Type genericType, int dbType, DocPropertyType docType) {

    String pgType = getPostgresType(postgres, dbType);
    if (Set.class.equals(type)) {
      return new OmSet(objectMapper, genericType, dbType, pgType, docType);
    }
    if (List.class.equals(type)) {
      return new OmList(objectMapper, genericType, dbType, pgType, docType);
    }
    if (Map.class.equals(type)) {
      return new OmMap(objectMapper, genericType, dbType, pgType);
    }
    return new GenericObject(objectMapper, genericType, dbType, pgType);
  }

  private static String getPostgresType(boolean postgres, int dbType) {
    if (postgres) {
      switch (dbType) {
        case DbPlatformType.JSON:
          return PostgresHelper.JSON_TYPE;
        case DbPlatformType.JSONB:
          return PostgresHelper.JSONB_TYPE;
      }
    }
    return null;
  }

  /**
   * Maps any type (Object) using Jackson ObjectMapper.
   */
  private static class GenericObject extends Base<Object> {

    public GenericObject(ObjectMapper objectMapper, Type type, int dbType, String pgType) {
      super(Object.class, objectMapper, type, dbType, pgType, DocPropertyType.OBJECT);
    }
  }

  /**
   * Type for Sets wrapping the ObjectMapper Set as a ModifyAwareSet.
   */
  @SuppressWarnings("rawtypes")
  private static class OmSet extends Base<Set> {

    public OmSet(ObjectMapper objectMapper, Type type, int dbType, String pgType, DocPropertyType docType) {
      super(Set.class, objectMapper, type, dbType, pgType, docType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set read(DataReader reader) throws SQLException {
      Set value = super.read(reader);
      return value == null ? null : new ModifyAwareSet(value);
    }
  }

  /**
   * Type for Lists wrapping the ObjectMapper List as a ModifyAwareList.
   */
  @SuppressWarnings("rawtypes")
  private static class OmList extends Base<List> {

    public OmList(ObjectMapper objectMapper, Type type, int dbType, String pgType, DocPropertyType docType) {
      super(List.class, objectMapper, type, dbType, pgType, docType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List read(DataReader reader) throws SQLException {
      List value = super.read(reader);
      return value == null ? null : new ModifyAwareList(value);
    }
  }

  /**
   * Type for Map wrapping the ObjectMapper Map as a ModifyAwareMap.
   */
  @SuppressWarnings("rawtypes")
  private static class OmMap extends Base<Map> {

    public OmMap(ObjectMapper objectMapper, Type type, int dbType, String pgType) {
      super(Map.class, objectMapper, type, dbType, pgType, DocPropertyType.OBJECT);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map read(DataReader reader) throws SQLException {
      Map value = super.read(reader);
      return value == null ? null : new ModifyAwareMap(value);
    }
  }

  /**
   * ScalarType that uses Jackson ObjectMapper to marshall/unmarshall to/from JSON
   * and storing them in one of JSON, JSONB, VARCHAR, CLOB or BLOB.
   */
  private static abstract class Base<T> extends ScalarTypeBase<T> {

    private final ObjectMapper objectMapper;

    private final JavaType javaType;

    private final String pgType;

    private final DocPropertyType docType;

    /**
     * Construct given the object mapper, property type and DB type for storage.
     */
    public Base(Class<T> cls, ObjectMapper objectMapper, Type type, int dbType, String pgType, DocPropertyType docType) {
      super(cls, false, dbType);
      this.pgType = pgType;
      this.docType = docType;
      this.objectMapper = objectMapper;
      this.javaType = objectMapper.getTypeFactory().constructType(type);
    }

    /**
     * Consider as a mutable type. Use the isDirty() method to check for dirty state.
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
    public T read(DataReader reader) throws SQLException {
      String json = reader.getString();
      if (json == null || json.isEmpty()) {
        return null;
      }
      try {
        return objectMapper.readValue(json, javaType);
      } catch (IOException e) {
        throw new SQLException("Unable to convert JSON", e);
      }
    }

    @Override
    public void bind(DataBind bind, T value) throws SQLException {
      if (pgType != null) {
        String rawJson = (value == null) ? null : formatValue(value);
        bind.setObject(PostgresHelper.asObject(pgType, rawJson));
      } else {
        if (value == null) {
          bind.setNull(Types.VARCHAR); // use varchar, otherwise SqlServer/db2 will fail with 'Invalid JDBC data type 5.001.'
        } else {
          try {
            String json = objectMapper.writeValueAsString(value);
            bind.setString(json);
          } catch (JsonProcessingException e) {
            throw new SQLException("Unable to create JSON", e);
          }
        }
      }
    }

    @Override
    public Object toJdbcType(Object value) {
      // no type conversion supported
      return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T toBeanType(Object value) {
      // no type conversion supported
      return (T) value;
    }

    @Override
    public String formatValue(T value) {
      try {
        return objectMapper.writeValueAsString(value);
      } catch (JsonProcessingException e) {
        throw new PersistenceException("Unable to create JSON", e);
      }
    }

    @Override
    public T parse(String value) {
      try {
        return objectMapper.readValue(value, javaType);
      } catch (IOException e) {
        throw new PersistenceException("Unable to convert JSON", e);
      }
    }

    @Override
    public DocPropertyType getDocType() {
      return docType;
    }

    @Override
    public boolean isDateTimeCapable() {
      return false;
    }

    @Override
    public T convertFromMillis(long dateTime) {
      throw new IllegalStateException("Not supported");
    }

    @Override
    public T jsonRead(JsonParser parser) throws IOException {
      return objectMapper.readValue(parser, javaType);
    }

    @Override
    public void jsonWrite(JsonGenerator writer, T value) throws IOException {
      objectMapper.writeValue(writer, value);
    }

    @Override
    public T readData(DataInput dataInput) throws IOException {
      if (!dataInput.readBoolean()) {
        return null;
      } else {
        return parse(dataInput.readUTF());
      }
    }

    @Override
    public void writeData(DataOutput dataOutput, T value) throws IOException {
      if (value == null) {
        dataOutput.writeBoolean(false);
      } else {
        ScalarHelp.writeUTF(dataOutput, format(value));
      }
    }
  }
}
