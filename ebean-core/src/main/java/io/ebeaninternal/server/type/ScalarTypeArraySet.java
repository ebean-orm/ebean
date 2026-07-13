package io.ebeaninternal.server.type;


import io.avaje.json.JsonReader;
import io.avaje.json.JsonWriter;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarType;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareSet;

import jakarta.persistence.PersistenceException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Type mapped for DB ARRAY type (Postgres only effectively).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class ScalarTypeArraySet extends ScalarTypeArrayBase<Set> implements ScalarTypeArray {

  static PlatformArrayTypeFactory factory() {
    return new Factory();
  }

  static class Factory implements PlatformArrayTypeFactory {

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, ScalarTypeArraySet> cache = new HashMap<>();

    /**
     * Return the ScalarType to use based on the List's generic parameter type.
     */
    @Override
    public ScalarType<?> typeFor(Type valueType, boolean nullable) {
      lock.lock();
      try {
        String key = valueType + ":" + nullable;
        if (valueType.equals(UUID.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySet(nullable, "uuid", DocPropertyType.UUID, ArrayElementConverter.UUID, UUID.class));
        }
        if (valueType.equals(Long.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySet(nullable, "bigint", DocPropertyType.LONG, ArrayElementConverter.LONG, Long.class));
        }
        if (valueType.equals(Integer.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySet(nullable, "integer", DocPropertyType.INTEGER, ArrayElementConverter.INTEGER, Integer.class));
        }
        if (valueType.equals(Double.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySet(nullable, "float", DocPropertyType.DOUBLE, ArrayElementConverter.DOUBLE, Double.class));
        }
        if (valueType.equals(String.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySet(nullable, "varchar", DocPropertyType.TEXT, ArrayElementConverter.STRING, String.class));
        }
        throw new IllegalArgumentException("Type [" + valueType + "] not supported for @DbArray mapping");
      } finally {
        lock.unlock();
      }
    }

    @Override
    public ScalarType<?> typeForEnum(ScalarType<?> scalarType, boolean nullable) {
      return new ScalarTypeArraySet(nullable, arrayTypeFor(scalarType), scalarType.docType(), new ArrayElementConverter.EnumConverter(scalarType), scalarType.type());
    }
  }

  private final String arrayType;
  private final ArrayElementConverter converter;
  private final Class<?> elementType;

  public ScalarTypeArraySet(boolean nullable, String arrayType, DocPropertyType docPropertyType, ArrayElementConverter converter, Class<?> elementType) {
    super(Set.class, Types.ARRAY, docPropertyType, nullable);
    this.arrayType = arrayType;
    this.converter = converter;
    this.elementType = elementType;
  }

  @Override
  public Class<?> elementType() {
    return elementType;
  }

  @Override
  public DocPropertyType docType() {
    return docPropertyType;
  }

  /**
   * Return the DB column definition for DDL generation.
   */
  @Override
  public String getDbColumnDefn() {
    return arrayType + "[]";
  }

  @Override
  protected Set fromArray(Object[] array1) {
    Set set = new LinkedHashSet();
    for (Object element : array1) {
      set.add(converter.fromDbArray(element));
    }
    return new ModifyAwareSet(set);
  }

  protected Object[] toArray(Set value) {
    return converter.toDbArray(value.toArray());
  }

  @Override
  public void bind(DataBinder binder, Set value) throws SQLException {
    if (value == null) {
      bindNull(binder);
    } else {
      binder.setArray(arrayType, toArray(value));
    }
  }

  @Override
  protected void bindNull(DataBinder binder) throws SQLException {
    if (nullable) {
      binder.setNull(Types.ARRAY);
    } else {
      binder.setArray(arrayType, EMPTY_ARRAY);
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
      return convert(EJson.parseList(value, false));
    } catch (IOException e) {
      throw new TextException("Failed to parse JSON [{}] as Set", value, e);
    }
  }

  /**
   * Convert from the json types to the proper scalar types (uuid, enum, double etc)
   */
  private Set convert(List<Object> rawList) {
    Set asSet = new LinkedHashSet();
    for (Object rawVal : rawList) {
        asSet.add(converter.fromSerialized(rawVal));
    }
    return new ModifyAwareSet(asSet);
  }

  @Override
  public Set jsonRead(JsonReader parser) throws IOException {
    if (parser.isNullValue()) {
      return null;
    }
    List<Object> rawList = EJson.parseList(parser, parser.currentToken());
    return convert(rawList);
  }

  @Override
  public void jsonWrite(JsonWriter writer, Set value) throws IOException {
    EJson.write(value, writer);
  }

}
