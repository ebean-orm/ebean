package io.ebean.avajejsonb.mapper;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.ebean.annotation.MutationDetection;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.JsonTrim;
import io.ebean.core.type.PostgresHelper;
import io.ebean.core.type.ScalarJsonManager;
import io.ebean.core.type.ScalarJsonMapper;
import io.ebean.core.type.ScalarJsonRequest;
import io.ebean.core.type.ScalarType;
import io.ebean.core.type.ScalarTypeBase;
import io.ebean.text.TextException;
import jakarta.persistence.PersistenceException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Supports {@code @DbJson} properties using Avaje Jsonb.
 */
public final class ScalarJsonAvajeJsonbMapper implements ScalarJsonMapper {

  private final ConcurrentHashMap<Type, JsonType<Object>> jsonTypes = new ConcurrentHashMap<>();

  @Override
  public <A extends Annotation> Class<A> markerAnnotation() {
    return null;
  }

  @Override
  public ScalarType<?> createType(ScalarJsonRequest request) {
    Type genericType = genericType(request);
    JsonType<Object> jsonType = jsonTypes.computeIfAbsent(genericType, type -> jsonb(request.manager()).type(type));
    if (request.mode() == MutationDetection.NONE) {
      return new NoMutationDetection(request.manager(), jsonType, request.dbType(), request.docType());
    }
    return new GenericObject(request.manager(), jsonType, request.dbType(), request.docType());
  }

  private Jsonb jsonb(ScalarJsonManager manager) {
    Object mapper = manager.mapper();
    return mapper instanceof Jsonb ? (Jsonb) mapper : Jsonb.instance();
  }

  private Type genericType(ScalarJsonRequest request) {
    Class<?> type = request.beanType();
    while (type != null) {
      try {
        Field field = type.getDeclaredField(request.name());
        return field.getGenericType();
      } catch (NoSuchFieldException e) {
        type = type.getSuperclass();
      }
    }
    throw new IllegalStateException("Field not found to match " + request.name());
  }

  private static final class NoMutationDetection extends Base<Object> {

    NoMutationDetection(ScalarJsonManager jsonManager, JsonType<Object> jsonType, int dbType, DocPropertyType docType) {
      super(Object.class, jsonManager, jsonType, dbType, docType);
    }
  }

  private static final class GenericObject extends Base<Object> {

    private final boolean jsonb;

    GenericObject(ScalarJsonManager jsonManager, JsonType<Object> jsonType, int dbType, DocPropertyType docType) {
      super(Object.class, jsonManager, jsonType, dbType, docType);
      this.jsonb = "jsonb".equals(pgType);
    }

    @Override
    public boolean mutable() {
      return true;
    }

    @Override
    public boolean jsonMapper() {
      return true;
    }

    @Override
    public Object read(DataReader reader) throws SQLException {
      String json = reader.getString();
      if (jsonb) {
        json = JsonTrim.trim(json);
      }
      reader.pushJson(json);
      return parseJson(json);
    }

    @Override
    public void bind(DataBinder binder, Object value) throws SQLException {
      String rawJson = binder.popJson();
      if (rawJson == null && value != null) {
        rawJson = formatValue(value);
      }
      bindJson(binder, value, rawJson);
    }
  }

  private static abstract class Base<T> extends ScalarTypeBase<T> {

    private final JsonType<T> jsonType;
    protected final String pgType;
    private final DocPropertyType docType;

    Base(Class<T> cls, ScalarJsonManager jsonManager, JsonType<T> jsonType, int dbType, DocPropertyType docType) {
      super(cls, false, dbType);
      this.jsonType = jsonType;
      this.pgType = jsonManager.postgresType(dbType);
      this.docType = docType;
    }

    @Override
    public T read(DataReader reader) throws SQLException {
      return parseJson(reader.getString());
    }

    @Override
    public void bind(DataBinder binder, T value) throws SQLException {
      bindJson(binder, value, value == null ? null : formatValue(value));
    }

    final T parseJson(String json) {
      if (json == null || json.isEmpty()) {
        return null;
      }
      try {
        return jsonType.fromJson(json);
      } catch (RuntimeException e) {
        throw new TextException("Failed to parse JSON [{}] as " + jsonType, json, e);
      }
    }

    final void bindJson(DataBinder binder, Object value, String rawJson) throws SQLException {
      if (pgType != null) {
        binder.setObject(PostgresHelper.asObject(pgType, rawJson));
      } else if (value == null) {
        binder.setNull(Types.VARCHAR);
      } else {
        binder.setString(rawJson);
      }
    }

    @Override
    public final Object toJdbcType(Object value) {
      return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final T toBeanType(Object value) {
      return (T) value;
    }

    @Override
    public final String formatValue(T value) {
      try {
        return jsonType.toJson(value);
      } catch (RuntimeException e) {
        throw new PersistenceException("Unable to create JSON", e);
      }
    }

    @Override
    public final T parse(String value) {
      return parseJson(value);
    }

    @Override
    public final DocPropertyType docType() {
      return docType;
    }

    @Override
    public final T jsonRead(JsonReader parser) {
      if (parser.isNullValue()) {
        return null;
      }
      return parseJson(parser.readRaw());
    }

    @Override
    public final void jsonWrite(JsonWriter writer, T value) throws IOException {
      if (value == null) {
        writer.nullValue();
      } else {
        writer.rawValue(formatValue(value));
      }
    }

    @Override
    public final T readData(DataInput dataInput) throws IOException {
      return dataInput.readBoolean() ? parse(dataInput.readUTF()) : null;
    }

    @Override
    public final void writeData(DataOutput dataOutput, T value) throws IOException {
      if (value == null) {
        dataOutput.writeBoolean(false);
      } else {
        dataOutput.writeBoolean(true);
        dataOutput.writeUTF(format(value));
      }
    }
  }
}
