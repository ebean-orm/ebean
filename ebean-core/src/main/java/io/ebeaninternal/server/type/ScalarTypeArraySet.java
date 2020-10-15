package io.ebeaninternal.server.type;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareSet;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.EMPTY_SET;

/**
 * Type mapped for DB ARRAY type (Postgres only effectively).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ScalarTypeArraySet extends ScalarTypeArrayBase<Set> implements ScalarTypeArray {

  static PlatformArrayTypeFactory factory() {
    return new Factory();
  }

  static class Factory implements PlatformArrayTypeFactory {

    private final ReentrantLock lock = new ReentrantLock(false);
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
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySet(nullable, "uuid", DocPropertyType.UUID, ArrayElementConverter.UUID));
        }
        if (valueType.equals(Long.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySet(nullable, "bigint", DocPropertyType.LONG, ArrayElementConverter.LONG));
        }
        if (valueType.equals(Integer.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySet(nullable, "integer", DocPropertyType.INTEGER, ArrayElementConverter.INTEGER));
        }
        if (valueType.equals(Double.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySet(nullable, "float", DocPropertyType.DOUBLE, ArrayElementConverter.DOUBLE));
        }
        if (valueType.equals(String.class)) {
          return cache.computeIfAbsent(key, s -> new ScalarTypeArraySet(nullable, "varchar", DocPropertyType.TEXT, ArrayElementConverter.STRING));
        }
        throw new IllegalArgumentException("Type [" + valueType + "] not supported for @DbArray mapping");
      } finally {
        lock.unlock();
      }
    }

    @Override
    public ScalarType<?> typeForEnum(ScalarType<?> scalarType, boolean nullable) {
      return new ScalarTypeArraySet(nullable, arrayTypeFor(scalarType), scalarType.getDocType(), new ArrayElementConverter.EnumConverter(scalarType));
    }
  }

  private final String arrayType;

  private final ArrayElementConverter converter;

  public ScalarTypeArraySet(boolean nullable, String arrayType, DocPropertyType docPropertyType, ArrayElementConverter converter) {
    super(Set.class, Types.ARRAY, docPropertyType, nullable);
    this.arrayType = arrayType;
    this.converter = converter;
  }

  @Override
  public DocPropertyType getDocType() {
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
      set.add(converter.toElement(element));
    }
    return new ModifyAwareSet(set);
  }

  protected Object[] toArray(Set value) {
    return converter.toDbArray(value.toArray());
  }

  @Override
  public void bind(DataBind bind, Set value) throws SQLException {
    if (value == null) {
      bindNull(bind);
    } else {
      bind.setArray(arrayType, toArray(value));
    }
  }

  @Override
  protected void bindNull(DataBind bind) throws SQLException {
    if (nullable) {
      bind.setNull(Types.ARRAY);
    } else {
      bind.setArray(arrayType, toArray(EMPTY_SET));
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
      return EJson.parseSet(value, false);
    } catch (IOException e) {
      throw new TextException("Failed to parse JSON [{}] as Set", value, e);
    }
  }

  @Override
  public Set jsonRead(JsonParser parser) throws IOException {
    return EJson.parseSet(parser, parser.getCurrentToken());
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Set value) throws IOException {
    EJson.write(value, writer);
  }

}
