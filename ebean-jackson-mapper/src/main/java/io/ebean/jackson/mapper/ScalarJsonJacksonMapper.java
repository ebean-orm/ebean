package io.ebean.jackson.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import io.ebean.annotation.MutationDetection;
import io.ebean.core.type.*;
import io.ebean.text.TextException;

import javax.persistence.PersistenceException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Supports @DbJson properties using Jackson ObjectMapper.
 */
public final class ScalarJsonJacksonMapper implements ScalarJsonMapper {

  private final Map<Class<?>, AnnotatedClass> jacksonClasses = new ConcurrentHashMap<>();

  @SuppressWarnings("unchecked")
  @Override
  public <A extends Annotation> Class<A> markerAnnotation() {
    return (Class<A>)com.fasterxml.jackson.annotation.JacksonAnnotation.class;
  }

  /**
   * Create and return the appropriate ScalarType.
   */
  @Override
  public ScalarType<?> createType(ScalarJsonRequest req) {
    AnnotatedClass annotatedClass = annotatedClass(req);
    AnnotatedField field = annotatedField(annotatedClass, req);
    if (req.mode() == MutationDetection.NONE) {
      return new NoMutationDetection(req.manager(), field, req.dbType(), req.docType());
    } else {
      return new GenericObject(req.manager(), field, req.dbType(), req.docType());
    }
  }

  private AnnotatedField annotatedField(AnnotatedClass annotatedClass, ScalarJsonRequest req) {
    for (AnnotatedField field : annotatedClass.fields()) {
      if (field.getName().equals(req.name())) {
        return field;
      }
    }
    throw new IllegalStateException("AnnotatedField not found to match " + req.name());
  }

  private AnnotatedClass annotatedClass(ScalarJsonRequest req) {
    return jacksonClasses.computeIfAbsent(req.beanType(), key -> {
      ObjectMapper mapper = (ObjectMapper) req.manager().mapper();
      return AnnotatedClassUtil.obtain(mapper, req.beanType());
    });
  }

  /**
   * No mutation detection on this json property.
   */
  private static final class NoMutationDetection extends Base<Object> {

    NoMutationDetection(ScalarJsonManager jsonManager, AnnotatedField field, int dbType, DocPropertyType docType) {
      super(Object.class, jsonManager, field, dbType, docType);
    }
  }

  /**
   * Supports HASH and SOURCE dirty detection modes.
   */
  private static final class GenericObject extends Base<Object> {

    private final boolean jsonb;

    GenericObject(ScalarJsonManager jsonManager, AnnotatedField field, int dbType, DocPropertyType docType) {
      super(Object.class, jsonManager, field, dbType, docType);
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
      // pushJson such that we MD5 and store on EntityBeanIntercept later
      reader.pushJson(json);
      if (json == null || json.isEmpty()) {
        return null;
      }
      try {
        return objectReader.readValue(json, deserType);
      } catch (IOException e) {
        throw new TextException("Failed to parse JSON [{}] as " + deserType, json, e);
      }
    }

    @Override
    public void bind(DataBinder binder, Object value) throws SQLException {
      // popJson as dirty detection already converted to json string
      String rawJson = binder.popJson();
      if (rawJson == null && value != null) {
        rawJson = formatValue(value); // not expected, need to check?
      }
      if (pgType != null) {
        binder.setObject(PostgresHelper.asObject(pgType, rawJson));
      } else {
        if (value == null) {
          // use varchar, otherwise SqlServer/db2 will fail with 'Invalid JDBC data type 5.001.'
          binder.setNull(Types.VARCHAR);
        } else {
          binder.setString(rawJson);
        }
      }
    }
  }

  /**
   * ScalarType that uses Jackson ObjectMapper to marshall/unmarshall to/from JSON
   * and storing them in one of JSON, JSONB, VARCHAR, CLOB or BLOB.
   */
  private static abstract class Base<T> extends ScalarTypeBase<T> {

    protected final ObjectWriter objectWriter;
    protected final ObjectMapper objectReader;
    protected final JavaType deserType;
    protected final String pgType;
    private final DocPropertyType docType;

    Base(Class<T> cls, ScalarJsonManager jsonManager, AnnotatedField field, int dbType, DocPropertyType docType) {
      super(cls, false, dbType);
      this.objectReader = (ObjectMapper)jsonManager.mapper();
      this.pgType = jsonManager.postgresType(dbType);
      this.docType = docType;
      final JacksonTypeHelper helper = new JacksonTypeHelper(field, objectReader);
      this.deserType = helper.type();
      this.objectWriter = helper.objectWriter();
    }

    @Override
    public T read(DataReader reader) throws SQLException {
      String json = reader.getString();
      if (json == null || json.isEmpty()) {
        return null;
      }
      try {
        return objectReader.readValue(json, deserType);
      } catch (IOException e) {
        throw new TextException("Failed to parse JSON [{}] as " + deserType, json, e);
      }
    }

    @Override
    public void bind(DataBinder binder, T value) throws SQLException {
      if (pgType != null) {
        String rawJson = (value == null) ? null : formatValue(value);
        binder.setObject(PostgresHelper.asObject(pgType, rawJson));
      } else {
        if (value == null) {
          binder.setNull(Types.VARCHAR); // use varchar, otherwise SqlServer/db2 will fail with 'Invalid JDBC data type 5.001.'
        } else {
          binder.setString(formatValue(value));
        }
      }
    }

    @Override
    public final Object toJdbcType(Object value) {
      // no type conversion supported
      return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final T toBeanType(Object value) {
      // no type conversion supported
      return (T) value;
    }

    @Override
    public final String formatValue(T value) {
      try {
        return objectWriter.writeValueAsString(value);
      } catch (JsonProcessingException e) {
        throw new PersistenceException("Unable to create JSON", e);
      }
    }

    @Override
    public final T parse(String value) {
      try {
        return objectReader.readValue(value, deserType);
      } catch (IOException e) {
        throw new TextException("Failed to parse JSON [{}] as " + deserType, value, e);
      }
    }

    @Override
    public final DocPropertyType docType() {
      return docType;
    }

    @Override
    public final T jsonRead(JsonParser parser) throws IOException {
      return objectReader.readValue(parser, deserType);
    }

    @Override
    public final void jsonWrite(JsonGenerator writer, T value) throws IOException {
      objectWriter.writeValue(writer, value);
    }

    @Override
    public final T readData(DataInput dataInput) throws IOException {
      if (!dataInput.readBoolean()) {
        return null;
      } else {
        return parse(dataInput.readUTF());
      }
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
